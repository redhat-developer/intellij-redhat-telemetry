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
import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.telemetryState;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TelemetryServiceTest {

    private ITelemetryService service;
    private TelemetryEvent event;
    private SegmentBroker broker;

    @BeforeEach
    public void before() {
        this.broker = createSegmentBroker();
        TelemetryState state = telemetryState(true);
        this.service = new TelemetryService(broker, state);
        this.event = new TelemetryEvent(null, "Testing Telemetry", null);
    }

    @Test
    public void send_should_send_if_is_enabled() {
        // given
        // when
        service.send(event);
        // then
        verify(broker).send(any(TelemetryEvent.class));
    }

    @Test
    public void send_should_NOT_send_if_is_NOT_enabled() {
        // given
        TelemetryService service = new TelemetryService(broker, telemetryState(false));
        // when
        service.send(new TelemetryEvent(null, "Testing Telemetry", null));
        // then
        verify(broker, never()).send(any(TelemetryEvent.class));
    }

    @Test
    public void setEnabled_true_should_send_all_event_that_are_on_hold() {
        // given
        TelemetryService service = new TelemetryService(broker, telemetryState(false));
        TelemetryEvent event1 = new TelemetryEvent(null, "Test1", null);
        TelemetryEvent event2 = new TelemetryEvent(null, "Test2", null);
        TelemetryEvent event3 = new TelemetryEvent(null, "Test3", null);
        service.send(event1);
        service.send(event2);
        service.send(event3);
        verify(broker, never()).send(any(TelemetryEvent.class));
        // when
        service.setEnabled(true);
        // then
        verify(broker, times(3)).send(any(TelemetryEvent.class));
    }

    private SegmentBroker createSegmentBroker() {
        return mock(SegmentBroker.class);
    }
}
