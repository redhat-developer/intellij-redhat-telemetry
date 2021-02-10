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

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.*;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type.*;

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

        public Sender actionPerformed(String name) {
            return new Sender(ACTION, name, service);
        }
    }

    public static class Sender {
        private final Type type;
        private final String name;
        private TelemetryService service;

        private Sender(Type type, String name, TelemetryService service) {
            this.type = type;
            this.name = name;
            this.service = service;
        }

        public void send() {
            TelemetryEvent event = new TelemetryEvent(type, name);
            service.send(event);
        }
    }
}
