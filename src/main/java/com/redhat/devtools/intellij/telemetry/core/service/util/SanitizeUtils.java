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
package com.redhat.devtools.intellij.telemetry.core.service.util;

import java.util.regex.Pattern;

public class SanitizeUtils {

    private static final Pattern USER_NAME_PATTERN = Pattern.compile(System.getProperty("user.name"));
    private static final String ANONYMOUS_USER_NAME = "<user>";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[^ ]+@[^ ]+");
    private static final String ANONYMOUS_EMAIL = "<email>";

    public static String replaceUserName(String string) {
        return USER_NAME_PATTERN.matcher(string).replaceAll(ANONYMOUS_USER_NAME);
    }

    public static String replaceEmail(String string) {
        return EMAIL_PATTERN.matcher(string).replaceAll(ANONYMOUS_EMAIL);
    }

}
