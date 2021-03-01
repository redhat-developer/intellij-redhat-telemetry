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
package com.redhat.devtools.intellij.telemetry.core.service.segment;

import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.service.Environment;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.TrackMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.environment;
import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.segmentConfiguration;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent.Type.ACTION;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_APP;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_NAME;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_OS;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SegmentBrokerTest {

    private static final String EXTENSION_NAME = "Telemetry by Red Hat";
    private static final String EXTENSION_VERSION = "extension-0.0.1";
    private static final String APPLICATION_NAME = SegmentBrokerTest.class.getSimpleName();
    private static final String APPLICATION_VERSION = "application-1.0.0";
    private static final String PLATFORM_NAME = "smurfOS";
    private static final String PLATFORM_DISTRIBUTION = "red hats";
    private static final String PLATFORM_VERSION = "0.1.0";
    private static final String USER_ID = "42";

    private Analytics analytics;
    private Environment environment;
    private SegmentBroker broker;
    private TelemetryEvent event;
    private ISegmentConfiguration configuration;

    @BeforeEach
    public void before() {
        this.analytics = createAnalytics();
        this.configuration = segmentConfiguration(false,"writeKey_value", "debugWriteKey_value");
        this.environment = environment(
                EXTENSION_NAME,
                EXTENSION_VERSION,
                APPLICATION_NAME,
                APPLICATION_VERSION,
                PLATFORM_NAME,
                PLATFORM_DISTRIBUTION,
                PLATFORM_VERSION);
        this.broker = new TestableSegmentBroker(false, USER_ID, environment, configuration, analytics);
        this.event = new TelemetryEvent(ACTION, "Testing Telemetry");
    }

    @Test
    public void send_should_enqueue_track_message_for_action_event() {
        // given
        // when
        broker.send(event);
        // then
        verify(analytics).enqueue(isA(TrackMessage.Builder.class));
    }

    @Test
    public void send_should_enqueue_track_message_with_userId() {
        // given
        ArgumentCaptor<MessageBuilder<?,?>> builder = ArgumentCaptor.forClass(MessageBuilder.class);
        // when
        broker.send(event);
        // then
        verify(analytics).enqueue(builder.capture());
        Message message = builder.getValue().build();
        assertThat(message.userId()).isEqualTo(USER_ID);
    }

    @Test
    public void send_should_NOT_enqueue_if_no_analytics() {
        // given
        IMessageBroker broker = new SegmentBroker(false, USER_ID, environment, configuration);
        // when
        broker.send(event);
        // then
        verify(analytics, never()).enqueue(any());
    }

    @Test
    public void send_should_enqueue_track_message_with_extension_and_application_names_and_versions() {
        // given
        ArgumentCaptor<MessageBuilder<?,?>> builder = ArgumentCaptor.forClass(MessageBuilder.class);
        // when
        broker.send(event);
        // then
        verify(analytics).enqueue(builder.capture());
        Map<String, ?> context = builder.getValue().build().context();
        assertThat(context).containsKey(PROP_APP);
        Map<String, String> appProperties = (Map<String, String>) context.get(PROP_APP);
        assertThat(appProperties.get(PROP_NAME)).isEqualTo(APPLICATION_NAME);
        assertThat(appProperties.get(PROP_VERSION)).isEqualTo(APPLICATION_VERSION);
        assertThat(context).containsKey(PROP_OS);
        Map<String, String> osProperties = (Map<String, String>) context.get(PROP_OS);
        assertThat(osProperties.get(PROP_NAME)).isEqualTo(PLATFORM_NAME);
        assertThat(osProperties.get(PROP_VERSION)).isEqualTo(PLATFORM_VERSION);
    }

    @Test
    public void dispose_should_flush_and_shutdown_analytics() {
        // given
        // when
        broker.dispose();
        // then
        verify(analytics).flush();
        verify(analytics).shutdown();
    }

    private Analytics createAnalytics() {
        return mock(Analytics.class);
    }

}
