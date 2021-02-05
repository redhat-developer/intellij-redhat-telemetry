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
import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.ITelemetryService;
import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;

public class TelemetryService implements ITelemetryService {

    public enum Type {
        ACTION, STARTUP, SHUTDOWN
    }

    private static final Logger LOGGER = Logger.getInstance(TelemetryService.class);

    private static final int BUFFER_SIZE = 35;

    private final IMessageBroker broker;
    private final CircularBuffer<TelemetryEvent> onHold = new CircularBuffer<>(BUFFER_SIZE);
    private final TelemetryState state;

    public TelemetryService() {
        this(new SegmentBroker(), ServiceManager.getService(TelemetryState.class));
    }

    public TelemetryService(IMessageBroker broker, TelemetryState state) {
        this.broker = broker;
        this.state = state;
    }

    @Override
    public void send(TelemetryEvent event) {
        if (isEnabled()) {
            flushOnHold();
            broker.send(event);
        } else {
            onHold.offer(event);
        }
    }

    private boolean isEnabled() {
        return state == null
                || state.isEnabled();
    }

    private void flushOnHold() {
        onHold.pollAll().forEach(broker::send);
    }

    public void dispose() {
        flushOnHold();
        onHold.clear();
        broker.dispose();
    }
}
