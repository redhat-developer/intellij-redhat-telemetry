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
import com.redhat.devtools.intellij.telemetry.core.service.Environment;
import com.redhat.devtools.intellij.telemetry.core.service.util.Lazy;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryService;
import com.redhat.devtools.intellij.telemetry.core.service.util.MapBuilder;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.PageMessage;
import com.segment.analytics.messages.TrackMessage;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SegmentBroker implements IMessageBroker {

    private static final Logger LOGGER = Logger.getInstance(SegmentBroker.class);

    public static final String PROP_NAME = "name";
    public static final String PROP_VERSION = "version";
    public static final String PROP_APP = "app";
    public static final String PROP_IP = "ip";
    public static final String PROP_COUNTRY = "country";
    public static final String PROP_LOCALE = "locale";
    public static final String PROP_LOCATION = "location";
    public static final String PROP_OS = "os";
    public static final String PROP_OS_NAME = "os_name";
    public static final String PROP_OS_VERSION = "os_version";
    public static final String PROP_TIMEZONE = "timezone";

    public static final String VALUE_NULL_IP = "0.0.0.0"; // fixed, faked ip addr

    public static final String PROP_EXTENSION_NAME = "extension_name";
    public static final String PROP_EXTENSION_VERSION = "extension_version";
    public static final String PROP_APP_NAME = "app_name";
    public static final String PROP_APP_VERSION = "app_version";

    private static final int FLUSH_INTERVAL = 10000;

    enum Type {
        IDENTIFY {
            public MessageBuilder toMessage(TelemetryEvent event, Map<String, Object> context, SegmentBroker broker) {
                return broker.toMessage(IdentifyMessage.builder(), event, context);
            }
        },
        TRACK {
            public MessageBuilder toMessage(TelemetryEvent event, Map<String, Object> context, SegmentBroker broker) {
                return broker.toMessage(TrackMessage.builder(event.getName()), event, context);
            }
        },
        PAGE {
            public MessageBuilder toMessage(TelemetryEvent event, Map<String, Object> context, SegmentBroker broker) {
                return broker.toMessage(PageMessage.builder(event.getName()), event, context);
            }

        };

        public abstract MessageBuilder toMessage(TelemetryEvent event, Map<String, Object> context, SegmentBroker broker);

        public static Type valueOf(TelemetryService.Type serviceType) {
            switch (serviceType) {
                case USER:
                    return IDENTIFY;
                case ACTION:
                case STARTUP:
                case SHUTDOWN:
                default:
                    return TRACK;
            }
        }
    }

    private final String anonymousId;
    private final Environment environment;
    protected Lazy<Analytics> analytics;

    public SegmentBroker(boolean isDebug, String anonymousId, Environment environment, ISegmentConfiguration configuration) {
        this.anonymousId = anonymousId;
        this.environment = environment;
        this.analytics = new Lazy<>(() -> createAnalytics(getWriteKey(isDebug, configuration)));
    }

    @Override
    public void send(TelemetryEvent event) {
        try {
            if (analytics.get() == null) {
                LOGGER.warn("Could not send " + event.getType() + " event '" + event.getName() + "': no analytics instance present.");
                return;
            }
            Map<String, Object> context = createContext(environment);
            Type type = Type.valueOf(event.getType());
            MessageBuilder builder = type.toMessage(event, context, this);
            LOGGER.debug("Sending message " + builder.type() + " to segment.");
            analytics.get().enqueue(builder);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not send " + event.getName() + " event: unknown type '" + event.getType() + "'.");
        }
    }

    private MessageBuilder toMessage(IdentifyMessage.Builder builder, TelemetryEvent event, Map<String, Object> context) {
        return builder
                .anonymousId(anonymousId)
                .traits(addTraitsEnvironment(event.getProperties()))
                .context(context);
    }

    private Map<String, ?> addTraitsEnvironment(Map<String, String> properties) {
        properties.put(PROP_LOCALE, environment.getLocale());
        properties.put(PROP_OS_NAME, environment.getPlatform().getName());
        properties.put(PROP_OS_VERSION, environment.getPlatform().getVersion());
        properties.put(PROP_TIMEZONE, environment.getTimezone());
        return properties;
    }

    private MessageBuilder toMessage(TrackMessage.Builder builder, TelemetryEvent event, Map<String, Object> context) {
        return builder
                .anonymousId(anonymousId)
                .properties(addIdentifyEnvironment(event.getProperties()))
                .context(context);
    }

    private Map<String, ?> addIdentifyEnvironment(Map<String, String> properties) {
        properties.put(PROP_APP_NAME, environment.getApplication().getName());
        properties.put(PROP_APP_VERSION, environment.getApplication().getVersion());
        properties.put(PROP_EXTENSION_NAME, environment.getPlugin().getName());
        properties.put(PROP_EXTENSION_VERSION, environment.getPlugin().getVersion());
        return properties;
    }

    private MessageBuilder toMessage(PageMessage.Builder builder, TelemetryEvent event, Map<String, Object> context) {
        return builder
                .anonymousId(anonymousId)
                .properties(event.getProperties())
                .context(context);
    }

    private Map<String, Object> createContext(Environment environment) {
        return MapBuilder.instance()
                .map(PROP_APP)
                .pair(PROP_NAME, environment.getApplication().getName())
                .pair(PROP_VERSION, environment.getApplication().getVersion())
                .build()
                .pair(PROP_IP, VALUE_NULL_IP)
                .pair(PROP_LOCALE, environment.getLocale())
                .map(PROP_LOCATION)
                .pair(PROP_COUNTRY, environment.getCountry())
                .build()
                .map(PROP_OS)
                .pair(PROP_NAME, environment.getPlatform().getName())
                .pair(PROP_VERSION, environment.getPlatform().getVersion())
                .build()
                .pair(PROP_TIMEZONE, environment.getTimezone())
                .build();
    }

    public void dispose() {
        analytics.get().flush();
        analytics.get().shutdown();
    }

    private Analytics createAnalytics(String writeKey) {

        if (writeKey == null) {
            LOGGER.warn("Could not create Segment Analytics instance, missing writeKey.");
            return null;
        }
        LOGGER.debug("Creating Segment Analytics instance using " + writeKey + " writeKey.");
        return Analytics.builder(writeKey)
                .flushQueueSize(1)
                .flushInterval(FLUSH_INTERVAL, TimeUnit.MILLISECONDS)
                .build();
    }

    public String getWriteKey(boolean isDebug, ISegmentConfiguration configuration) {
        if (isDebug) {
            return configuration.getSegmentDebugKey();
        } else {
            return configuration.getSegmentNormalKey();
        }
    }

}
