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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.service.Event.Type;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;
import com.redhat.devtools.intellij.telemetry.core.util.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.ACTION;
import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.SHUTDOWN;
import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.STARTUP;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymize;
import static com.redhat.devtools.intellij.telemetry.core.util.TimeUtils.toLocalTime;

public class TelemetryMessageBuilder {

    private static final Logger LOGGER = Logger.getInstance(TelemetryMessageBuilder.class);

    private final TelemetryServiceFacade service;

    public TelemetryMessageBuilder(ClassLoader classLoader) {
        this(new TelemetryServiceFacade(classLoader));
    }

    TelemetryMessageBuilder(TelemetryServiceFacade serviceFacade) {
        this.service = serviceFacade;
    }

    public ActionMessage action(String name) {
        return new ActionMessage(name, service);
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
        static final String PROP_ERROR = "error";
        static final String PROP_RESULT = "result";

        public static final String RESULT_SUCCESS = "success";

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

        public ActionMessage success() {
            return result(RESULT_SUCCESS);
        }

        public ActionMessage result(String result) {
            property(PROP_RESULT, result);
            return clearError();
        }

        protected ActionMessage clearResult() {
            properties().remove(PROP_RESULT);
            return this;
        }

        String getResult() {
            return getProperty(PROP_RESULT);
        }

        public ActionMessage error(Exception exception) {
            if (exception == null) {
                return this;
            }
            return error(exception.getMessage());
        }

        public ActionMessage error(String message) {
            property(PROP_ERROR, anonymize(message));
            return clearResult();
        }

        protected ActionMessage clearError() {
            properties().remove(PROP_ERROR);
            return this;
        }

        String getError() {
            return getProperty(PROP_ERROR);
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

        protected TelemetryServiceFacade(final ClassLoader classLoader) {
            this(() -> ApplicationManager.getApplication().getService(TelemetryServiceFactory.class).create(classLoader));
        }

        protected TelemetryServiceFacade(final Supplier<IService> supplier) {
            super(supplier);
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
            MessageBusConnection connection = createMessageBusConnection();
            connection.subscribe(AppLifecycleListener.TOPIC, new AppLifecycleListener() {
                @Override
                public void appWillBeClosed(boolean isRestart) {
                    sendShutdown();
                }
            });
        }

        protected void sendShutdown() {
            new ShutdownMessage(TelemetryServiceFacade.this).send();
        }

        protected MessageBusConnection createMessageBusConnection() {
            return ApplicationManager.getApplication().getMessageBus().connect();
        }

        @Override
        public void send(Event event) {
            get().send(event);
        }
    }
}
