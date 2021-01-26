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
    private TrackEvent event;
    private SegmentBroker broker;

    @BeforeEach
    public void before() {
        this.broker = createSegmentBroker();
        TelemetryState state = telemetryState(true);
        this.service = new TelemetryService(broker, state);
        this.event = new TrackEvent( "Testing Telemetry", service);
    }

    @Test
    public void send_should_send_if_is_enabled() {
        // given
        // when
        service.send(event);
        // then
        verify(broker).send(any(TrackEvent.class));
    }

    @Test
    public void send_should_NOT_send_if_is_NOT_enabled() {
        // given
        TelemetryService service = new TelemetryService(broker, telemetryState(false));
        // when
        service.send(new TrackEvent( "Testing Telemetry", service));
        // then
        verify(broker, never()).send(any(TrackEvent.class));
    }

    @Test
    public void setEnabled_true_should_send_all_event_that_are_on_hold() {
        // given
        TelemetryService service = new TelemetryService(broker, telemetryState(false));
        TrackEvent event1 = new TrackEvent( "Test1", service);
        TrackEvent event2 = new TrackEvent( "Test2", service);
        TrackEvent event3 = new TrackEvent( "Test3", service);
        service.send(event1);
        service.send(event2);
        service.send(event3);
        verify(broker, never()).send(any(TrackEvent.class));
        // when
        service.setEnabled(true);
        // then
        verify(broker, times(3)).send(any(TrackEvent.class));
    }

    private SegmentBroker createSegmentBroker() {
        return mock(SegmentBroker.class);
    }
}
