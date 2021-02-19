/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.util;

import java.util.regex.Pattern;

public class AnonymizeUtils {

    private static final String USER_NAME = System.getProperty("user.name");
    private static final String HOME_DIR = System.getProperty("user.home");
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String ANONYMOUS_USER_NAME = "$USER";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}",
            Pattern.CASE_INSENSITIVE);
    private static final String ANONYMOUS_EMAIL = "$EMAIL";
    private static final Pattern IP_PATTERN = Pattern.compile(
            "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
    private static final String ANONYMOUS_IP = "$IP";

    public static String anonymize(String string) {
        return replaceEmail(
                replaceUserName(
                        replaceIP(
                                replaceHomedir(
                                        replaceTmpdir(string)
                                )
                        )
                )
        );
    }

    public static String replaceUserName(String string) {
        if (string == null
                || string.isEmpty()) {
            return string;
        }
        return string.replace(USER_NAME, ANONYMOUS_USER_NAME);
    }

    public static String replaceEmail(String string) {
        if (string == null
                || string.isEmpty()) {
            return string;
        }
        return EMAIL_PATTERN.matcher(string).replaceAll(ANONYMOUS_EMAIL);
    }

    public static String replaceHomedir(String string) {
        if (string == null
                || string.isEmpty()) {
            return string;
        }
        return string.replace(HOME_DIR, "$HOMEDIR");
    }

    public static String replaceTmpdir(String string) {
        if (string == null
                || string.isEmpty()) {
            return string;
        }
        return string.replace(TMP_DIR, "$TMPDIR");
    }

    public static String replaceIP(String string) {
        if (string == null
                || string.isEmpty()) {
            return string;
        }
        return IP_PATTERN.matcher(string).replaceAll(ANONYMOUS_IP);
    }

}
