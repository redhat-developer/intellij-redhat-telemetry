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

import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.IdentifyTraitsPersistence;
import com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker;
import com.redhat.devtools.intellij.telemetry.ui.TelemetryNotifications;
import com.redhat.devtools.intellij.telemetry.util.BlockingFlush;
import com.redhat.devtools.intellij.telemetry.util.StdOutLogging;
import com.segment.analytics.Analytics;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.environment;
import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.segmentConfiguration;
import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.telemetryConfiguration;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.FeedbackServiceFacade;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.TelemetryServiceFacade;
import static org.mockito.Mockito.mock;

@Disabled("For manual testing purposes only")
class TelemetryMessageBuilderIntegrationTest {

    private static final String EXTENSION_NAME = "com.redhat.devtools.intellij.telemetry";
    private static final String EXTENSION_VERSION = "1.0.0.44";
    private static final String APPLICATION_VERSION = "1.0.0";
    private static final String APPLICATION_NAME = TelemetryMessageBuilderIntegrationTest.class.getSimpleName();
    private static final String PLATFORM_NAME = "smurfOS";
    private static final String PLATFORM_DISTRIBUTION = "red hats";
    private static final String PLATFORM_VERSION = "0.1.0";
    private static final String LOCALE = "de_CH";
    private static final String TIMEZONE = "Europe/Bern";
    private static final String COUNTRY = "Switzerland";
    public static final String SEGMENT_WRITE_KEY = "XXXXXXXXXXXXXXX";
    public static final String SEGMENT_DEBUG_WRITE_KEY = "YYYYYYYYYYYYYYY";

    private BlockingFlush blockingFlush;
    private Analytics analytics;
    private TelemetryMessageBuilder messageBuilder;

    @BeforeEach
    void before() {
        this.blockingFlush = BlockingFlush.create();
        this.analytics = createAnalytics(blockingFlush, createClient());
        ISegmentConfiguration segmentConfiguration = segmentConfiguration(SEGMENT_WRITE_KEY, SEGMENT_DEBUG_WRITE_KEY);
        Environment environment = environment(
                APPLICATION_NAME,
                APPLICATION_VERSION,
                EXTENSION_NAME,
                EXTENSION_VERSION,
                PLATFORM_NAME,
                PLATFORM_DISTRIBUTION,
                PLATFORM_VERSION,
                LOCALE,
                TIMEZONE,
                COUNTRY);
        SegmentBroker broker = new SegmentBroker(
                false,
                UserId.INSTANCE.get(),
                IdentifyTraitsPersistence.INSTANCE,
                environment,
                segmentConfiguration,
                key -> analytics);
        TelemetryConfiguration telemetryConfiguration = telemetryConfiguration(TelemetryConfiguration.Mode.DEBUG);

        TelemetryService telemetryService = new TelemetryService(
                telemetryConfiguration,
                broker,
                mock(MessageBusConnection.class),
                mock(TelemetryNotifications.class));
        IService telemetryServiceFacade = new TelemetryServiceFacade(() -> telemetryService, mock(MessageBusConnection.class));

        FeedbackService feedbackService = new FeedbackService(broker);
        IService feedbackServiceFacade = new FeedbackServiceFacade(() -> feedbackService);

        this.messageBuilder = new TelemetryMessageBuilder(telemetryServiceFacade, feedbackServiceFacade);
    }

    @AfterEach
    void after() {
        shutdownAnalytics();
    }

    @Test
    void should_send_telemetry() {
        // given
        // when
        messageBuilder.action("testing-telemetry")
                .property("Wicked sorcerer", "Gargamel")
                .property("Cat", "Azrael")
                .success()
                .send();
        // then
    }

    @Test
    void should_send_feedback() {
        // given
        // when
        messageBuilder.feedback("testing-feedback")
                .property("Jedi", "Luke Skywalker")
                .property("Sith", "Darth Vader")
                .send();
        // then
    }

    private Analytics createAnalytics(BlockingFlush blockingFlush, OkHttpClient client) {
        return Analytics.builder(SEGMENT_WRITE_KEY)
                .flushQueueSize(1)
                .plugin(new StdOutLogging())
                .plugin(blockingFlush.plugin())
                .client(client)
                .build();
    }

    private OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    private void shutdownAnalytics() {
        analytics.flush();
        blockingFlush.block();
        analytics.shutdown();
    }
}
