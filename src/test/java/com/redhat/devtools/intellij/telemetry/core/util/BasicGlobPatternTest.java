/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.util;

import org.junit.jupiter.api.Test;

import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasicGlobPatternTest {

    @Test
    public void compile_should_throw_when_range_is_invalid() {
        // given, when, then
        assertThrows(PatternSyntaxException.class, () -> BasicGlobPattern.compile("[5-1] jedi"));
    }

    @Test
    public void compile_should_throw_when_brace_expansions_are_nested() {
        // given, when, then
        assertThrows(PatternSyntaxException.class, () -> BasicGlobPattern.compile("{{yoda,obiwan}"));
    }

    @Test
    public void compile_should_throw_when_brace_expansions_are_not_closed() {
        // given, when, then
        assertThrows(PatternSyntaxException.class, () -> BasicGlobPattern.compile("{yoda,obiwan"));
    }

    @Test
    public void machtes_should_match_expression_that_starts_and_ends_with_wildcard() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("*yoda*");
        // when, then
        assertThat(glob.matches("master yoda is a jedi master")).isTrue();
        assertThat(glob.matches("yoda")).isTrue(); // * matches no character, too
        assertThat(glob.matches("master yoda")).isTrue(); // * matches no character, too
        assertThat(glob.matches("master obiwan is a jedi master, too")).isFalse();
    }

    @Test
    public void machtes_should_match_expression_that_starts_with_wildcard() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("*yoda");
        // when, then
        assertThat(glob.matches("master yoda")).isTrue();
        assertThat(glob.matches("yoda")).isTrue();
        assertThat(glob.matches("master obiwan")).isFalse();
    }

    @Test
    public void machtes_should_match_expression_that_has_a_wildcard() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("y*da");
        // when, then
        assertThat(glob.matches("yoda")).isTrue();
        assertThat(glob.matches("yooooda")).isTrue();
    }

    @Test
    public void machtes_should_match_expression_that_starts_and_ends_with_placeholder() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("?this is yoda?");
        // when, then
        assertThat(glob.matches("!this is yoda!")).isTrue();
        assertThat(glob.matches("!!this is yoda!")).isFalse();
        assertThat(glob.matches("this is yoda!")).isFalse();
    }

    @Test
    public void machtes_should_match_expression_that_has_placeholders() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("y??a");
        // when, then
        assertThat(glob.matches("yoda")).isTrue();
        assertThat(glob.matches("yiza")).isTrue();
        assertThat(glob.matches("yoooda")).isFalse();
    }

    @Test
    public void machtes_should_match_expression_with_brace_expansions() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("{yoda,obiwan,skywalker} is a jedi");
        // when, then
        assertThat(glob.matches("yoda is a jedi")).isTrue();
        assertThat(glob.matches("obiwan is a jedi")).isTrue();
        assertThat(glob.matches("skywalker is a jedi")).isTrue();
        assertThat(glob.matches("darthvader is a jedi")).isFalse();
    }

    @Test
    public void machtes_should_match_empty_brace_expansion() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("{yoda,darth,} the jedi");
        // when, then
        assertThat(glob.matches("yoda the jedi")).isTrue();
        assertThat(glob.matches(" the jedi")).isTrue(); // empty alternative
    }

    @Test
    public void machtes_should_match_expression_with_a_range() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("jedi [0-4]");
        // when, then
        assertThat(glob.matches("jedi 0")).isTrue();
        assertThat(glob.matches("jedi 1")).isTrue();
        assertThat(glob.matches("jedi 4")).isTrue();
        assertThat(glob.matches("jedi 5")).isFalse();
    }

    @Test
    public void machtes_should_match_expression_with_alternatives() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("jedi [abc]");
        // when, then
        assertThat(glob.matches("jedi a")).isTrue();
        assertThat(glob.matches("jedi b")).isTrue();
        assertThat(glob.matches("jedi c")).isTrue();
        assertThat(glob.matches("jedi d")).isFalse();
    }

    @Test
    public void machtes_should_match_parenthesis_and_pipe_as_normal_characters() {
        // given
        BasicGlobPattern glob = BasicGlobPattern.compile("jedi(s|42)");
        // when, then
        assertThat(glob.matches("jedi(s|42)")).isTrue();
        assertThat(glob.matches("jedi(s)")).isFalse();
    }
}
