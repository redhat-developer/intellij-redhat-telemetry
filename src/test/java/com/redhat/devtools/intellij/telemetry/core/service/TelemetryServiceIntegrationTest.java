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

import com.jakewharton.retrofit.Ok3Client;
import com.redhat.devtools.intellij.telemetry.core.ITelemetryService;
import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;
import com.redhat.devtools.intellij.telemetry.util.BlockingFlush;
import com.redhat.devtools.intellij.telemetry.util.StdOutLogging;
import com.segment.analytics.Analytics;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit.client.Client;

import java.util.concurrent.TimeUnit;

import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.environment;
import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.telemetryState;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type.*;

public class TelemetryServiceIntegrationTest {

    private static final String EXTENSION_NAME = "com.redhat.devtools.intellij.telemetry";
    private static final String EXTENSION_VERSION = "0.0.1";
    private static final String APPLICATION_VERSION = "1.0.0";
    private static final String APPLICATION_NAME = TelemetryServiceIntegrationTest.class.getSimpleName();
    public static final String SEGMENT_WRITE_KEY = "HYuMCHlIpTvukCKZA42OubI1cvGIAap6";

    private BlockingFlush blockingFlush;
    private Analytics analytics;
    private ITelemetryService service;
    private TelemetryEvent event;

    @BeforeEach
    public void before() {
        this.blockingFlush = BlockingFlush.create();
        this.analytics = createAnalytics(blockingFlush, createClient());
        Environment environment = environment(APPLICATION_NAME, APPLICATION_VERSION, EXTENSION_NAME, EXTENSION_VERSION);
        SegmentBroker broker = new SegmentBroker(UserId.INSTANCE.get(), analytics, environment);
        TelemetryState state = telemetryState(true);
        this.service = new TelemetryService(broker, state);
        this.event = new TelemetryEvent(ACTION, "Testing Telemetry");
    }

    @AfterEach
    public void after() {
        shutdownAnalytics();
    }

    private void shutdownAnalytics() {
        analytics.flush();
        blockingFlush.block();
        analytics.shutdown();
    }


    @Test
    public void should_send_track_event() {
        // given
        // when
        service.send(event);
        // then
    }

    private Analytics createAnalytics(BlockingFlush blockingFlush, Client client) {
        return Analytics.builder(SEGMENT_WRITE_KEY)
                .plugin(new StdOutLogging())
                .plugin(blockingFlush.plugin())
                .client(client)
                .build();
    }

    private Client createClient() {
        return new Ok3Client(
                new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .build());
    }

}
