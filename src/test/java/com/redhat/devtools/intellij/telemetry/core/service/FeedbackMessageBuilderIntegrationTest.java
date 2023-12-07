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
import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.IdentifyTraitsPersistence;
import com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker;
import com.redhat.devtools.intellij.telemetry.util.BlockingFlush;
import com.redhat.devtools.intellij.telemetry.util.StdOutLogging;
import com.segment.analytics.Analytics;
import okhttp3.OkHttpClient;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit.client.Client;

import java.util.concurrent.TimeUnit;

import static com.redhat.devtools.intellij.telemetry.core.service.Fakes.segmentConfiguration;
import static com.redhat.devtools.intellij.telemetry.core.service.FeedbackMessageBuilder.*;
import static org.mockito.Mockito.mock;

@Ignore("For manual testing purposes only")
class FeedbackMessageBuilderIntegrationTest {

    public static final String SEGMENT_WRITE_KEY = "ySk3bh8S8hDIGVKX9FQ1BMGOdFxbsufn";

    private BlockingFlush blockingFlush;
    private Analytics analytics;
    private FeedbackMessage message;

    @BeforeEach
    void before() {
        this.blockingFlush = BlockingFlush.create();
        this.analytics = createAnalytics(blockingFlush, createClient());
        ISegmentConfiguration configuration = segmentConfiguration(SEGMENT_WRITE_KEY, "");
        SegmentBroker broker = new SegmentBroker(
                false,
                UserId.INSTANCE.get(),
                IdentifyTraitsPersistence.INSTANCE,
                null,
                configuration,
                key -> analytics);
        IService service = new FeedbackService(broker);
        this.message = new FeedbackMessageBuilder(service)
                .feedback("Testing Feedback");
    }

    @AfterEach
    void after() {
        shutdownAnalytics();
    }

    @Test
    void should_send_track_event() {
        // given
        message.property("jedi", "yoda")
            .property("use", "the force");
        // when
        message.send();
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

    private void shutdownAnalytics() {
        analytics.flush();
        blockingFlush.block();
        analytics.shutdown();
    }

}
