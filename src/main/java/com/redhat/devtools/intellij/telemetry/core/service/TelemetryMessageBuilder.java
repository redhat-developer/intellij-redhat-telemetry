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

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.Event.Type;
import com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBrokerFactory;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;
import com.redhat.devtools.intellij.telemetry.core.util.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.ACTION;
import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.SHUTDOWN;
import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.STARTUP;
import static com.redhat.devtools.intellij.telemetry.core.util.TimeUtils.toLocalTime;

public class TelemetryMessageBuilder {

    private final IService telemetryFacade;
    private final IService feedbackFacade;

    public TelemetryMessageBuilder(PluginDescriptor descriptor) {
        this(new SegmentBrokerFactory().create(TelemetryConfiguration.getInstance().isDebug(), descriptor));
    }

    TelemetryMessageBuilder(IMessageBroker messageBroker) {
        this(
            new TelemetryServiceFacade(TelemetryConfiguration.getInstance(), messageBroker),
            new FeedbackServiceFacade(messageBroker)
        );
    }

    TelemetryMessageBuilder(IService telemetryFacade, IService feedbackFacade) {
        this.telemetryFacade = telemetryFacade;
        this.feedbackFacade = feedbackFacade;
    }

    public ActionMessage action(String name) {
        return new ActionMessage(name, telemetryFacade);
    }

    public FeedbackMessage feedback(String name) {
        return new FeedbackMessage(name, feedbackFacade);
    }

    static class StartupMessage extends TelemetryMessage<StartupMessage> {

        private StartupMessage(IService service) {
            super(STARTUP, "startup", service);
        }
    }

    static class ShutdownMessage extends TelemetryMessage<ShutdownMessage> {

        private static final String PROP_SESSION_DURATION = "session_duration";

        private ShutdownMessage(IService service) {
            this(toLocalTime(ApplicationManager.getApplication().getStartTime()), service);
        }

        private ShutdownMessage(LocalDateTime startup, IService service) {
            this(startup, LocalDateTime.now(), service);
        }

        ShutdownMessage(LocalDateTime startup, LocalDateTime shutdown, IService service) {
            super(SHUTDOWN, "shutdown", service);
            sessionDuration(startup, shutdown);
        }

        private ShutdownMessage sessionDuration(LocalDateTime startup, LocalDateTime shutdown) {
            return sessionDuration(Duration.between(startup, shutdown));
        }

        private ShutdownMessage sessionDuration(Duration duration) {
            return property(PROP_SESSION_DURATION, TimeUtils.toString(duration));
        }

        String getSessionDuration() {
            return getProperty(PROP_SESSION_DURATION);
        }
    }

    public static class ActionMessage extends TelemetryMessage<ActionMessage> {

        static final String PROP_DURATION = "duration";

        private LocalDateTime started;

        private ActionMessage(String name, IService service) {
            super(ACTION, name, service);
            started();
        }

        public ActionMessage started() {
            return started(LocalDateTime.now());
        }

        public ActionMessage started(LocalDateTime started) {
            this.started = started;
            return this;
        }

        public ActionMessage finished() {
            finished(LocalDateTime.now());
            return this;
        }

        public ActionMessage finished(LocalDateTime finished) {
            duration(Duration.between(started, finished));
            return this;
        }

        public ActionMessage duration(Duration duration) {
            return property(PROP_DURATION, TimeUtils.toString(duration));
        }

        String getDuration() {
            return getProperty(PROP_DURATION);
        }

        @Override
        public Event send() {
            ensureFinished();
            ensureResultOrError();
            return super.send();
        }

        private void ensureFinished() {
            if (!hasProperty(PROP_DURATION)) {
                finished();
            }
        }

        private void ensureResultOrError() {
            if (!hasProperty(PROP_ERROR)
                    && !hasProperty(PROP_RESULT)) {
                success();
            }
        }
    }

    private static class TelemetryMessage<M extends TelemetryMessage<?>> extends Message<M> {
        protected TelemetryMessage(Type type, String name, IService service) {
            super(type, name, service);
        }
    }

    static class TelemetryServiceFacade extends Lazy<IService> implements IService {

        private final MessageBusConnection messageBusConnection;

        protected TelemetryServiceFacade(final TelemetryConfiguration configuration, IMessageBroker broker) {
            this(() -> ApplicationManager.getApplication().getService(TelemetryServiceFactory.class).create(configuration, broker),
                    ApplicationManager.getApplication().getMessageBus().connect()
            );
        }

        protected TelemetryServiceFacade(final Supplier<IService> supplier, MessageBusConnection connection) {
            super(supplier);
            this.messageBusConnection = connection;
        }

        @Override
        protected void onCreated(IService service) {
            sendStartup();
            onShutdown();
        }

        private void sendStartup() {
            new StartupMessage(this).send();
        }

        private void onShutdown() {
            messageBusConnection.subscribe(AppLifecycleListener.TOPIC, new AppLifecycleListener() {
                @Override
                public void appWillBeClosed(boolean isRestart) {
                    sendShutdown();
                }
            });
        }

        protected void sendShutdown() {
            new ShutdownMessage(TelemetryServiceFacade.this).send();
        }

        @Override
        public void send(Event event) {
            get().send(event);
        }
    }

    static class FeedbackServiceFacade extends Lazy<IService> implements IService {

        protected FeedbackServiceFacade(final IMessageBroker broker) {
            this(() -> ApplicationManager.getApplication().getService(FeedbackServiceFactory.class).create(broker));
        }

        protected FeedbackServiceFacade(final Supplier<IService> supplier) {
            super(supplier);
        }

        @Override
        public void send(Event event) {
            get().send(event);
        }
    }

    public static class FeedbackMessage extends Message<FeedbackMessage>{

        FeedbackMessage(String name, IService service) {
            super(ACTION, name, service);
        }
    }

}
