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

import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.Environment;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryService;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.PageMessage;
import com.segment.analytics.messages.TrackMessage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SegmentBroker implements IMessageBroker {

    private static final Logger LOGGER = Logger.getInstance(SegmentBroker.class);

    public static final String PROP_EXTENSION_NAME = "extension_name";
    public static final String PROP_EXTENSION_VERSION = "extension_version";
    public static final String PROP_APPLICATION_NAME = "application_name";
    public static final String PROP_APPLICATION_VERSION = "application_version";

    private static final int FLUSH_INTERVAL = 10000;

    enum Type {
        IDENTIFY {
            MessageBuilder builder(String name) {
                return IdentifyMessage.builder();
            }
        },
        TRACK {
            MessageBuilder builder(String name) {
                return TrackMessage.builder(name);
            }
        },
        PAGE {
            MessageBuilder builder(String name) {
                return PageMessage.builder(name);
            }
        };

        abstract MessageBuilder builder(String name);

        public static Type valueOf(TelemetryService.Type serviceEventType) {
            switch (serviceEventType) {
                case ACTION:
                case STARTUP:
                case SHUTDOWN:
                default:
                    return TRACK;
            }
        }
    }

    private final String anonymousId;
    private final ISegmentConfiguration configuration;
    protected Analytics analytics;

    public SegmentBroker(String anonymousId, ISegmentConfiguration configuration) {
        this.anonymousId = anonymousId;
        this.configuration = configuration;
    }

    @Override
    public void send(TelemetryEvent event) {
        try {
            if (analytics == null) {
                LOGGER.warn("Could not send " + event.getType() + " event '" + event.getName() + "': no analytics instance present.");
                return;
            }
            HashMap<String, String> context = createContext(event.getEnvironment());
            MessageBuilder builder = toMessage(event, context);
            send(builder);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not send " + event.getName() + " event: unknown type '" + event.getType() + "'.");
        }
    }

    public void send(MessageBuilder builder) {
        LOGGER.debug("Sending message " + builder.type() + " to segment.");
        analytics.enqueue(builder);
    }

    private MessageBuilder toMessage(TelemetryEvent event, Map<String, String> context) {
        Type segmentType = Type.valueOf(event.getType());
        MessageBuilder builder = segmentType.builder(event.getName());
        return builder
                .anonymousId(anonymousId)
                .context(context);
    }

    private HashMap<String, String> createContext(Environment environment) {
        HashMap<String, String> context = new HashMap<>();
        context.put(PROP_EXTENSION_NAME, environment.getExtension().getName());
        context.put(PROP_EXTENSION_VERSION, environment.getExtension().getVersion());
        context.put(PROP_APPLICATION_NAME, environment.getApplication().getName());
        context.put(PROP_APPLICATION_VERSION, environment.getApplication().getVersion());
        return context;
    }

    public void dispose() {
        analytics.flush();
        analytics.shutdown();
    }

    protected Analytics getAnalytics(String writeKey) {
        if (analytics == null) {
            this.analytics = createAnalytics(writeKey);
        }
        return analytics;
    }

    private Analytics createAnalytics(String writeKey) {
        if (writeKey == null) {
            LOGGER.warn("Could not create Segment Analytics instance, missing writeKey");
            return null;
        }
        return Analytics.builder(writeKey)
                .flushQueueSize(1)
                .flushInterval(FLUSH_INTERVAL, TimeUnit.MILLISECONDS)
                .build();
    }
}
