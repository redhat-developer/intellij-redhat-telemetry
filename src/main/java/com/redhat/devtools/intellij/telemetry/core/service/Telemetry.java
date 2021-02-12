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

import com.intellij.openapi.components.ServiceManager;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type.ACTION;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type.STARTUP;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type.USER;

public class Telemetry {

    public static ServiceBuilder service(ClassLoader classLoader) {
        return new ServiceBuilder(classLoader);
    }

    public static class ServiceBuilder {

        private final TelemetryService service;

        private ServiceBuilder(final ClassLoader classLoader) {
            TelemetryServiceFactory factory = ServiceManager.getService(TelemetryServiceFactory.class);
            this.service = factory.create(classLoader);
        }

        public UserMessage user() {
            return new UserMessage(service);
        }

        public ActionMessage actionPerformed(String name) {
            return new ActionMessage(ACTION, name, service);
        }

        public ActionMessage startupPerformed(String name) {
            return new ActionMessage(STARTUP, name, service);
        }

        public ActionMessage shutdownPerformed(String name) {
            return new ActionMessage(STARTUP, name, service);
        }
    }

    public static class ActionMessage extends Message<ActionMessage> {

        private final String PROP_DURATION = "duration";
        private final String PROP_ERROR = "error";
        private final String PROP_RESULT = "result";

        private final LocalTime startTime;

        private ActionMessage(Type type, String name, TelemetryService service) {
            super(type, name, service);
            this.startTime = LocalTime.now();
        }

        public ActionMessage finished() {
            return duration(Duration.between(startTime, LocalTime.now()));
        }

        public ActionMessage duration(Duration duration) {
            return property(PROP_DURATION, String.format("%02d:%02d:%02d",
                    duration.toHours(),
                    duration.toMinutes() % 60,
                    duration.getSeconds() % 60));
        }

        public ActionMessage success() {
            return success("success");
        }

        public ActionMessage success(String message) {
            return property(PROP_RESULT, message);
        }

        public ActionMessage error(String message) {
            return property(PROP_ERROR, message);
        }

        public ActionMessage error(Exception exception) {
            return property(PROP_ERROR, exception.getMessage());
        }

        private void ensureDuration() {
            if (hasKey(PROP_DURATION)) {
                finished();
            }
        }

        private void ensureSuccessOrError() {
            if (hasKey(PROP_ERROR)
                    && hasKey(PROP_RESULT)) {
                success();
            }
        }

    }

    public static class UserMessage extends Message<UserMessage> {

        private UserMessage(TelemetryService service) {
            super(USER, "Anonymous ID: " + AnonymousId.INSTANCE.get(), service);
        }
    }

    abstract static class Message<T extends Message> {

        private final Type type;
        private final Map<String, String> properties = new HashMap<>();
        private TelemetryService service;

        private Message(Type type, String name, TelemetryService service) {
            this.type = type;
            this.service = service;
        }

        public T property(String key, String value) {
            properties.put(key, value);
            return (T) this;
        }

        public void send() {
            String name = AnonymousId.INSTANCE.get();
            TelemetryEvent event = new TelemetryEvent(type, name, properties);
            service.send(event);
        }

        protected boolean hasKey(String key) {
            return properties.containsKey(key);
        }
    }

}
