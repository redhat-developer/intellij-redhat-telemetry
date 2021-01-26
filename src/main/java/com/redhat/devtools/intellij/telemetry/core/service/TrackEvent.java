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

public class TrackEvent extends TelemetryEvent {

    private static final String TYPE = "track";

    public TrackEvent(String name) {
        this(name, ServiceManager.getService(ITelemetryService.class));
    }

    TrackEvent(String name, ITelemetryService service) {
        super(TYPE, name, service);
    }

    @Override
    public void send() {
        this.service.send(this);
    }

    void send(IEventBroker broker) {
        broker.send(this);
    }
}
