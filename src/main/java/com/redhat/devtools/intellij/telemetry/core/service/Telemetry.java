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
import com.redhat.devtools.intellij.telemetry.core.ITelemetryService;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.*;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type.*;

public class Telemetry {

    public static MessageBuilder environment(Object emitter) {
        Environment environment = Environment.builder().emitter(emitter).build();
        return new MessageBuilder(environment);
    }

    public static MessageBuilder environment(Environment environment) {
        return new MessageBuilder(environment);
    }

    public static class MessageBuilder {

        private Environment environment;

        private MessageBuilder(Environment environment) {
            this.environment = environment;
        }

        public Sender actionPerformed(String event) {
            return new Sender(ACTION, event, environment);
        }
    }

    public static class Sender {
        private final Environment environment;
        private final Type type;
        private final String name;
        private ITelemetryService service;

        private Sender(Type type, String name, Environment environment) {
            this.type = type;
            this.name = name;
            this.environment = environment;
        }

        public void send() {
            TelemetryEvent event = new TelemetryEvent(type, name, environment);
            getService().send(event);
        }

        private static ITelemetryService getService() {
            return ServiceManager.getService(ITelemetryService.class);
        }
    }
}
