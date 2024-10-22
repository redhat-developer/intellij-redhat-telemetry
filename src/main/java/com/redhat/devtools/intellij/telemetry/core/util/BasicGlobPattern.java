/*************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * License: GNU General Public License version 2 plus the Classpath exception
 *
 * Based on implementation at sun.nio.fs.Globs in jdk 11.0.2
 *
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.util;

import com.intellij.openapi.util.text.StringUtil;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A simple Glob Pattern that supports:
 * <ul>
 *     <li>placeholders {@code ?}</li>
 *     <li>wildcard {@code *}</li>
 *     <li>brace expansions {@code {alternative1,alternative2,}}</li>
 *     <li>ranges {@code [1-4]}</li>
 * </ul>
 * It does not support extended (advanced-, posix-) glob expressions like alternatives {@code @(a|b) or +(a|b) etc )}
 */
public class BasicGlobPattern {

    private static final String regexMetaChars = ".^$+{[]|()";
    private static final String globMetaChars = "\\*?[{";

    private final Pattern globPattern;

    public static BasicGlobPattern compile(String glob) {
        return new Factory().create(glob);
    }

    private BasicGlobPattern(Pattern globPattern) {
        this.globPattern = globPattern;
    }

    public boolean matches(String toMatch) {
        if (StringUtil.isEmpty(toMatch)) {
            return false;
        }
        return globPattern.matcher(toMatch).matches();
    }

    private static final class Factory {

        private static final class GlobParserContext {

            public static final char EOL = 0;

            private final String globPattern;
            private final StringBuilder builder = new StringBuilder("^");

            private boolean inGroup = false;
            private boolean hasRangeStart = false;
            private int index = 0;
            private char lastRangeCharacter = 0;

            GlobParserContext(String globPattern) {
                this.globPattern = globPattern;
            }

            String getGlobPattern() {
                return globPattern;
            }

            int getGlobIndex() {
                return index;
            }

            boolean globEndReached() {
                return index >= globPattern.length();
            }

            char peekGlob() {
                if (index < globPattern.length()) {
                    return globPattern.charAt(index);
                }
                return EOL;
            }

            char pollGlob() {
                if (index < globPattern.length()) {
                    return globPattern.charAt(index++);
                }
                return EOL;
            }

            void nextGlobChar() {
                index++;
            }

            GlobParserContext appendToRegex(String toAppend) {
                builder.append(toAppend);
                return this;
            }

            GlobParserContext appendToRegex(char toAppend) {
                builder.append(toAppend);
                return this;
            }

            GlobParserContext setInGroup(boolean inGroup) {
                this.inGroup = inGroup;
                return this;
            }

            boolean isInGroup() {
                return inGroup;
            }

            GlobParserContext setInRange(boolean hasRangeStart) {
                this.hasRangeStart = hasRangeStart;
                return this;
            }

            boolean isInRange() {
                return hasRangeStart;
            }

            void setLastRangeCharacter(char character) {
                this.lastRangeCharacter = character;
            }

            char getLastRangeCharacter() {
                return lastRangeCharacter;
            }

            String getRegex() {
                return builder.toString();
            }
        }

        private BasicGlobPattern create(String glob) {
            Pattern globPattern = createRegex(glob);
            return new BasicGlobPattern(globPattern);
        }

        private Pattern createRegex(String globPattern) {
            if (globPattern == null) {
                return null;
            }
            GlobParserContext context = new GlobParserContext(globPattern);
            while (!context.globEndReached()) {
                char c = context.pollGlob();
                switch (c) {
                    case '\\':
                        handleEscape(context);
                        break;
                    case '/':
                        context.appendToRegex(c);
                        break;
                    case '[':
                        handleSquareOpen(context);
                        break;
                    case '{':
                        handleCurlyOpen(context);
                        break;
                    case '}':
                        handleCurlyClose(context);
                        break;
                    case ',':
                        handleComma(context);
                        break;
                    case '*':
                        handleWildcard(context);
                        break;
                    case '?':
                        handleQuestionMark(context);
                        break;

                    default:
                        handleDefaultCharacter(c, context);
                }
            }

            if (context.isInGroup()) {
                throw new PatternSyntaxException("Missing '}", globPattern, context.getGlobIndex() - 1);
            }

            context.appendToRegex('$');
            return Pattern.compile(context.getRegex());
        }

        private void handleEscape(GlobParserContext context) {
            // escape special characters
            if (context.globEndReached()) {
                throw new PatternSyntaxException("No character to escape", context.getGlobPattern(), context.getGlobIndex() - 1);
            }
            char next = context.pollGlob();
            if (isGlobMeta(next) || isRegexMeta(next)) {
                context.appendToRegex('\\');
            }
            context.appendToRegex(next);
        }

        private void handleSquareOpen(GlobParserContext context) {
            char character = '[';
            // don't match name separator in class
            context.appendToRegex("[[^/]&&[");
            if (context.peekGlob() == '^') {
                // escape the regex negation char if it appears
                context.appendToRegex("\\^").nextGlobChar();
            } else {
                // negation
                if (context.peekGlob() == '!') {
                    context.appendToRegex('^').nextGlobChar();
                }
                // hyphen allowed at start
                if (context.peekGlob() == '-') {
                    context.appendToRegex('-').nextGlobChar();
                }
            }
            while (!context.globEndReached()) {
                character = context.pollGlob();
                if (character == ']') {
                    break;
                }
                if (character == '/') {
                    throw new PatternSyntaxException("Explicit 'name separator' in class", context.getGlobPattern(), context.getGlobIndex() - 1);
                }
                // TBD: how to specify ']' in a class?
                if (character == '\\' || character == '[' ||
                        character == '&' && context.peekGlob() == '&') {
                    // escape '\', '[' or "&&" for regex class
                    context.appendToRegex('\\');
                }
                context.appendToRegex(character);

                if (character == '-') {
                    if (!context.isInRange()) {
                        throw new PatternSyntaxException("Invalid range", context.getGlobPattern(), context.getGlobIndex() - 1);
                    }
                    character = context.pollGlob();
                    if (character == GlobParserContext.EOL || character == ']') {
                        break;
                    }
                    if (character < context.getLastRangeCharacter()) {
                        throw new PatternSyntaxException("Invalid range", context.getGlobPattern(), context.getGlobIndex() - 3);
                    }
                    context.appendToRegex(character).setInRange(false);
                } else {
                    context.setInRange(true).setLastRangeCharacter(character);
                }
            }
            if (character != ']') {
                throw new PatternSyntaxException("Missing ']", context.getGlobPattern(), context.getGlobIndex() - 1);
            }
            context.appendToRegex("]]");
        }

        private void handleCurlyOpen(GlobParserContext context) {
            if (context.isInGroup()) {
                throw new PatternSyntaxException("Cannot nest groups", context.getGlobPattern(), context.getGlobIndex() - 1);
            }
            context.setInGroup(true).appendToRegex("(?:(?:");
        }

        private void handleCurlyClose(GlobParserContext context) {
            if (context.isInGroup()) {
                context.appendToRegex("))").setInGroup(false);
            } else {
                context.appendToRegex('}');
            }
        }

        private void handleComma(GlobParserContext context) {
            if (context.isInGroup()) {
                context.appendToRegex(")|(?:");
            } else {
                context.appendToRegex(',');
            }
        }

        private void handleWildcard(GlobParserContext context) {
            if (context.peekGlob() == '*') {
                // crosses directory boundaries
                context.appendToRegex(".*").nextGlobChar();
            } else {
                // within directory boundary
                context.appendToRegex("[^/]*");
            }
        }

        private void handleQuestionMark(GlobParserContext context) {
            context.appendToRegex("[^/]");
        }

        private void handleDefaultCharacter(char c, GlobParserContext context) {
            if (isRegexMeta(c)) {
                context.appendToRegex('\\');
            }
            context.appendToRegex(c);
        }

        private boolean isRegexMeta(char c) {
            return regexMetaChars.indexOf(c) != -1;
        }

        private boolean isGlobMeta(char c) {
            return globMetaChars.indexOf(c) != -1;
        }
    }
}
