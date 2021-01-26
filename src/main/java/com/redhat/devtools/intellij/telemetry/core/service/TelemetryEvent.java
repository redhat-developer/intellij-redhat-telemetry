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

import com.intellij.openapi.components.ServiceManager;
import com.redhat.devtools.intellij.telemetry.core.IEventBroker;
import com.redhat.devtools.intellij.telemetry.core.ITelemetryService;

public abstract class TelemetryEvent {

    private final String type;
    private final String name;
    protected final ITelemetryService service;

    protected TelemetryEvent(String type, String name, ITelemetryService service) {
        this.type = type;
        this.name = name;
        this.service = service;
    }

    abstract public void send();

    abstract void send(IEventBroker broker);

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
