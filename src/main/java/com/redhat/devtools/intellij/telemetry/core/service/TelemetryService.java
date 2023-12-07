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

import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.KEY_MODE;

import java.util.concurrent.atomic.AtomicBoolean;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.ConfigurationChangedListener;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.Mode;
import com.redhat.devtools.intellij.telemetry.core.service.Event.Type;
import com.redhat.devtools.intellij.telemetry.core.util.CircularBuffer;
import com.redhat.devtools.intellij.telemetry.ui.TelemetryNotifications;

public class TelemetryService implements IService {

    private static final Logger LOGGER = Logger.getInstance(TelemetryService.class);

    private static final int BUFFER_SIZE = 35;

    private final TelemetryNotifications notifications;
    private final TelemetryConfiguration configuration;
    protected final IMessageBroker broker;
    private final AtomicBoolean userQueried = new AtomicBoolean(false);
    private final CircularBuffer<Event> onHold = new CircularBuffer<>(BUFFER_SIZE);

    public TelemetryService(final TelemetryConfiguration configuration, final IMessageBroker broker) {
        this(configuration, broker, ApplicationManager.getApplication().getMessageBus().connect(), new TelemetryNotifications());
    }

    TelemetryService(
            final TelemetryConfiguration configuration,
            final IMessageBroker broker,
            final MessageBusConnection connection,
            final TelemetryNotifications notifications) {
        this.configuration = configuration;
        this.broker = broker;
        this.notifications = notifications;
        onConfigurationChanged(connection);
    }

    private void onConfigurationChanged(MessageBusConnection connection) {
        connection.subscribe(ConfigurationChangedListener.CONFIGURATION_CHANGED, (String key, String value) -> {
            if (KEY_MODE.equals(key)
                    && Mode.safeValueOf(value).isEnabled()) {
                flushOnHold();
            }
        });
    }

    @Override
    public void send(Event event) {
        sendUserInfo();
        doSend(event);
        queryUserConsent();
    }

    private void sendUserInfo() {
        doSend(new Event(
                Type.USER,
                "Anonymous ID: " + UserId.INSTANCE.get()));
    }

    private void queryUserConsent() {
        if (!isConfigured()
                && userQueried.compareAndSet(false, true)) {
            notifications.queryUserConsent();
        }
    }

    private void doSend(Event event) {
        if (isEnabled()) {
            flushOnHold();
            broker.send(event);
        } else if (!isConfigured()) {
            onHold.offer(event);
        }
    }

    private boolean isEnabled() {
        return configuration != null
                && configuration.isEnabled();
    }

    private boolean isConfigured() {
        return configuration != null
                && configuration.isConfigured();
    }

    private void flushOnHold() {
        onHold.pollAll().forEach(this::send);
    }

    public void dispose() {
        flushOnHold();
        onHold.clear();
        broker.dispose();
    }
}
