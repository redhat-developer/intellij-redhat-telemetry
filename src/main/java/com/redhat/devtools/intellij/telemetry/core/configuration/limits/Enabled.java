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
package com.redhat.devtools.intellij.telemetry.core.configuration.limits;

import com.intellij.openapi.util.text.StringUtil;

import java.util.Arrays;

public enum Enabled {
    ALL("all"),
    ERROR("error"),
    CRASH("crash"),
    OFF("off");

    private final String value;

    Enabled(String value) {
        this.value = value;
    }

    private boolean hasValue(String value) {
        if (StringUtil.isEmptyOrSpaces(value)) {
            return this.value == null;
        }
        return value.equals(this.value);
    }

    public static Enabled safeValueOf(String value) {
        return Arrays.stream(values())
                .filter(instance -> instance.hasValue(value))
                .findAny()
                .orElse(ALL);
    }
}