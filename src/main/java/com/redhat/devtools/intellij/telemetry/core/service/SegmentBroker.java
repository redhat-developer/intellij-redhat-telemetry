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

import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.AnalyticsFactory;
import com.redhat.devtools.intellij.telemetry.core.IEventBroker;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.TrackMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redhat.devtools.intellij.telemetry.core.service.Environment.*;

public class SegmentBroker implements IEventBroker {

    private static final Logger LOGGER = Logger.getInstance(SegmentBroker.class);

    public static final String PROP_EXTENSION_NAME = "extension_name";
    public static final String PROP_EXTENSION_VERSION = "extension_version";
    public static final String PROP_APPLICATION_NAME = "application_name";
    public static final String PROP_APPLICATION_VERSION = "application_version";

    private final String anonymousId;
    private final Environment environment;
    private final Analytics analytics;

    public SegmentBroker() {
        this(UserId.INSTANCE.get(), AnalyticsFactory.INSTANCE.create(), new EnvironmentBuilder().build());
    }

    SegmentBroker(String anonymousId, Analytics analytics, Environment environment) {
        this.anonymousId = anonymousId;
        this.environment = environment;
        this.analytics = analytics;
    }

    @Override
    public void send(TrackEvent event) {
        if (analytics == null) {
            LOGGER.warn("Could not send " + event.getType() + " event '" + event.getName() + "': no analytics instance present.");
            return;
        }

        MessageBuilder builder = toMessage(event, createContext(environment));
        LOGGER.debug("Sending message " + builder.type() + " to segment." );
        analytics.enqueue(builder);
    }

    private MessageBuilder toMessage(TrackEvent event, Map<String, String> context) {
        MessageBuilder builder = TrackMessage.builder(event.getName());
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
}
