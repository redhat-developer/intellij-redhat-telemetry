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
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.TrackMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.function.Function;

import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.environment;
import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.segmentConfiguration;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent.Type.ACTION;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent.Type.SHUTDOWN;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent.Type.STARTUP;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent.Type.USER;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_APP;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_APP_NAME;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_APP_VERSION;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_EXTENSION_NAME;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_EXTENSION_VERSION;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_LOCALE;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_NAME;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_OS;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_OS_DISTRIBUTION;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_OS_NAME;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_OS_VERSION;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_TIMEZONE;
import static com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker.PROP_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
    private static final String LOCALE = "de_CH";
    private static final String TIMEZONE = "Europe/Bern";
    public static final String NORMAL_WRITE_KEY = "writeKey_value";
    public static final String DEBUG_WRITE_KEY = "debugWriteKey_value";

    private Analytics analytics;
    private Environment environment;
    private SegmentBroker broker;
    private TelemetryEvent actionEvent;
    private TelemetryEvent userEvent;
    private TelemetryEvent startupEvent;
    private TelemetryEvent shutdownEvent;
    private ISegmentConfiguration configuration;

    @BeforeEach
    public void before() {
        this.analytics = createAnalytics();
        this.configuration = segmentConfiguration(NORMAL_WRITE_KEY, DEBUG_WRITE_KEY);
        this.environment = environment(
                EXTENSION_NAME,
                EXTENSION_VERSION,
                APPLICATION_NAME,
                APPLICATION_VERSION,
                PLATFORM_NAME,
                PLATFORM_DISTRIBUTION,
                PLATFORM_VERSION,
                LOCALE,
                TIMEZONE);
        this.broker = new SegmentBroker(false, USER_ID, environment, configuration, key -> analytics);
        this.actionEvent = new TelemetryEvent(ACTION, "Action event");
        this.userEvent = new TelemetryEvent(USER, "User event");
        this.startupEvent = new TelemetryEvent(STARTUP, "Startup event");
        this.shutdownEvent = new TelemetryEvent(SHUTDOWN, "Startup event");
    }

    @Test
    public void should_create_analytics_with_normal_key_if_is_not_debug() {
        // given
        // lambda cannot be spied on, function instance can
        Function<String, Analytics> analyticsFactory = spy(new Function<String, Analytics>() {
            @Override
            public Analytics apply(String key) {
                return analytics;
            }
        });
        SegmentBroker broker = new SegmentBroker(false, USER_ID, environment, configuration, analyticsFactory);
        // when
        broker.send(actionEvent);
        // then
        verify(analyticsFactory).apply(NORMAL_WRITE_KEY);
    }

    @Test
    public void should_create_analytics_with_debug_key_if_is_debug() {
        // given
        // lambda cannot be spied on, function instance can
        Function<String, Analytics> analyticsFactory = spy(new Function<String, Analytics>() {
            @Override
            public Analytics apply(String key) {
                return analytics;
            }
        });
        SegmentBroker broker = new SegmentBroker(true, USER_ID, environment, configuration, analyticsFactory);
        // when
        broker.send(actionEvent);
        // then
        verify(analyticsFactory).apply(DEBUG_WRITE_KEY);
    }

    @Test
    public void send_should_enqueue_track_message_for_action_event() {
        // given
        // when
        broker.send(actionEvent);
        // then
        verify(analytics).enqueue(isA(TrackMessage.Builder.class));
    }

    @Test
    public void send_should_enqueue_track_message_with_userId() {
        // given
        ArgumentCaptor<MessageBuilder<?,?>> builder = ArgumentCaptor.forClass(MessageBuilder.class);
        // when
        broker.send(actionEvent);
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
        broker.send(actionEvent);
        // then
        verify(analytics, never()).enqueue(any());
    }

    @Test
    public void send_should_enqueue_message_with_context() {
        // given
        ArgumentCaptor<MessageBuilder<?,?>> builder = ArgumentCaptor.forClass(MessageBuilder.class);
        // when
        broker.send(actionEvent);
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
    public void send_should_enqueue_track_message_for_startup_event() {
        // given
        // when
        broker.send(startupEvent);
        // then
        verify(analytics).enqueue(isA(TrackMessage.Builder.class));
    }

    @Test
    public void send_should_enqueue_track_message_for_shutdown_event() {
        // given
        // when
        broker.send(shutdownEvent);
        // then
        verify(analytics).enqueue(isA(TrackMessage.Builder.class));
    }

    @Test
    public void send_should_enqueue_identify_message_for_user_event() {
        // given
        // when
        broker.send(userEvent);
        // then
        verify(analytics).enqueue(isA(IdentifyMessage.Builder.class));
    }

    @Test
    public void send_should_enqueue_identify_message_with_traits() {
        // given
        ArgumentCaptor<IdentifyMessage.Builder> builder = ArgumentCaptor.forClass(IdentifyMessage.Builder.class);
        // when
        broker.send(userEvent);
        // then
        verify(analytics).enqueue(builder.capture());
        Map<String, ?> traits = builder.getValue().build().traits();
        assertThat(traits.get(PROP_LOCALE)).isEqualTo(LOCALE);
        assertThat(traits.get(PROP_TIMEZONE)).isEqualTo(TIMEZONE);
        assertThat(traits.get(PROP_OS_NAME)).isEqualTo(PLATFORM_NAME);
        assertThat(traits.get(PROP_OS_DISTRIBUTION)).isEqualTo(PLATFORM_DISTRIBUTION);
        assertThat(traits.get(PROP_OS_VERSION)).isEqualTo(PLATFORM_VERSION);
    }

    @Test
    public void send_should_enqueue_track_message_with_properties() {
        // given
        ArgumentCaptor<TrackMessage.Builder> builder = ArgumentCaptor.forClass(TrackMessage.Builder.class);
        // when
        broker.send(actionEvent);
        // then
        verify(analytics).enqueue(builder.capture());
        Map<String, ?> traits = builder.getValue().build().properties();
        assertThat(traits.get(PROP_APP_NAME)).isEqualTo(APPLICATION_NAME);
        assertThat(traits.get(PROP_APP_VERSION)).isEqualTo(APPLICATION_VERSION);
        assertThat(traits.get(PROP_EXTENSION_NAME)).isEqualTo(EXTENSION_NAME);
        assertThat(traits.get(PROP_EXTENSION_VERSION)).isEqualTo(EXTENSION_VERSION);
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
