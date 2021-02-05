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
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.ITelemetryService;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;

import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.*;

public class TelemetryService implements ITelemetryService {

    public enum Type {
        ACTION, STARTUP, SHUTDOWN
    }

    private static final Logger LOGGER = Logger.getInstance(TelemetryService.class);

    private static final int BUFFER_SIZE = 35;

    private final IMessageBroker broker;
    private final CircularBuffer<TelemetryEvent> onHold = new CircularBuffer<>(BUFFER_SIZE);
    private final TelemetryConfiguration configuration;

    public TelemetryService() {
        this(new SegmentBroker(), INSTANCE);
    }

    /**
     * Creates a telemetry service for the given broker and state. Present for testing purposes.
     * <p>
     * "Deprecated" annotation is required to avoid IDEA IC-2019.3+ complaining about non-empty constructor.
     * Starting with IC-2019.3 there mustn't be (additional) service constructors with parameters.
     * It'll throw a PluginException otherwise:
     * <pre>
     * com.intellij.diagnostic.PluginException:
     * getComponentAdapterOfType is used to get com.redhat.devtools.intellij.telemetry.core.IEventBroker (requestorClass=com.redhat.devtools.intellij.telemetry.core.service.TelemetryService,
     * requestorConstructor=com.redhat.devtools.intellij.telemetry.core.service.TelemetryService(com.redhat.devtools.intellij.telemetry.core.IEventBroker,com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState)).
     * Probably constructor should be marked as NonInjectable. [Plugin: com.redhat.devtools.intellij.telemetry]
     * </pre>
     *
     * @see "https://git.jetbrains.org/?p=idea%2Fcommunity.git&a=search&st=commit&s=getComponentAdapterOfType"
     * @see "https://intellij-support.jetbrains.com/hc/en-us/community/posts/360005805879-PluginException-getComponentAdapterOfType-is-used-to-get-XXX-Probably-constructor-should-be-marked-as-NonInjectable-"
     * @see "https://jira.sonarsource.com/browse/SLI-342"
     * @see "https://github.com/SonarSource/sonarlint-intellij/commit/3cec478aa25ba45ca7b40587eba1ebc953787ac9#diff-c2ad48b42854127c9594a3f58d706856ee1e1c14b5e0053bc8a4dea4f212041eR58"
     */
    @Deprecated
    public TelemetryService(IMessageBroker broker, TelemetryConfiguration configuration) {
        this.broker = broker;
        this.configuration = configuration;
    }

    @Override
    public void send(TelemetryEvent event) {
        if (isEnabled()) {
            flushOnHold();
            broker.send(event);
        } else {
            onHold.offer(event);
        }
    }

    private boolean isEnabled() {
        return configuration == null
                || configuration.getMode() != Mode.DISABLED;
    }

    private void flushOnHold() {
        onHold.pollAll().forEach(broker::send);
    }

    public void dispose() {
        flushOnHold();
        onHold.clear();
        broker.dispose();
    }
}
