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
package com.redhat.devtools.intellij.telemetry.core.service;

import com.redhat.devtools.intellij.telemetry.core.ITelemetryService;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.*;

public class TelemetryEvent {

    private final Type type;
    private final String name;
    private final Environment environment;
    protected ITelemetryService service;

    protected TelemetryEvent(Type type, String name, Environment environment) {
        this.type = type;
        this.name = name;
        this.environment = environment;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
