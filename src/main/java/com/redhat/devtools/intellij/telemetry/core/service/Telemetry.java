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

        public ActionSender actionPerformed(String name) {
            return new ActionSender(ACTION, name, service);
        }

        public ActionSender startupPerformed(String name) {
            return new ActionSender(STARTUP, name, service);
        }

        public ActionSender shutdownPerformed(String name) {
            return new ActionSender(STARTUP, name, service);
        }
    }

    public static class ActionSender {

        private final String PROP_DURATION = "duration";
        private final String PROP_ERROR = "error";
        private final String PROP_RESULT = "result";

        private final Type type;
        private final String name;
        private final Map<String, String> properties = new HashMap<>();
        private final LocalTime startTime;
        private TelemetryService service;

        private ActionSender(Type type, String name, TelemetryService service) {
            this.type = type;
            this.name = name;
            this.startTime = LocalTime.now();
            this.service = service;
        }

        public ActionSender finished() {
            return duration(Duration.between(startTime, LocalTime.now()));
        }

        public ActionSender duration(Duration duration) {
            return property(PROP_DURATION, String.format("%02d:%02d:%02d",
                    duration.toHours(),
                    duration.toMinutes() % 60,
                    duration.getSeconds() % 60));
        }

        public ActionSender success() {
            return success("success");
        }

        public ActionSender success(String message) {
            return property(PROP_RESULT, message);
        }

        public ActionSender error(String message) {
            return property(PROP_ERROR, message);
        }

        public ActionSender error(Exception exception) {
            return property(PROP_ERROR, exception.getMessage());
        }

        public ActionSender property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public void send() {
            ensureDuration();
            ensureSuccessOrError();
            TelemetryEvent event = new TelemetryEvent(type, name, properties);
            service.send(event);
        }

        private void ensureDuration() {
            if (properties.get(PROP_DURATION) == null) {
                finished();
            }
        }

        private void ensureSuccessOrError() {
            if (properties.get(PROP_ERROR) == null
                    && properties.get(PROP_RESULT) == null) {
                success();
            }
        }

    }
}
