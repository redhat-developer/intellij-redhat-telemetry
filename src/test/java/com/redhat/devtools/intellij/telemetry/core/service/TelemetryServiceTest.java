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
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.telemetryConfiguration;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TelemetryServiceTest {

    private ITelemetryService service;
    private TelemetryEvent event;
    private SegmentBroker broker;

    @BeforeEach
    public void before() {
        this.broker = createSegmentBroker();
        TelemetryConfiguration configuration = telemetryConfiguration(true);
        this.service = new TestableTelemetryService(configuration, broker);
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
        TelemetryConfiguration configuration = telemetryConfiguration(false);
        TelemetryService service = new TestableTelemetryService(configuration, broker);
        // when
        service.send(new TelemetryEvent(null, "Testing Telemetry", null));
        // then
        verify(broker, never()).send(any(TelemetryEvent.class));
    }

    private SegmentBroker createSegmentBroker() {
        return mock(SegmentBroker.class);
    }

}
