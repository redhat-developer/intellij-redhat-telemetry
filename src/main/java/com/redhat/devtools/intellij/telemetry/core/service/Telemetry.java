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

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryService.Type.*;

public class Telemetry {

    public static TelemetryBuilder actionPerformed(String name) {
        return new TelemetryBuilder(ACTION, name);
    }

    public static class TelemetryBuilder {
        private TelemetryService.Type type;
        private String name;
        private ITelemetryService service;

        private TelemetryBuilder(TelemetryService.Type type, String name) {
            this.type = type;
            this.name = name;
        }

        public void send() {
            TelemetryEvent event = new TelemetryEvent(type, name);
            getService().send(event);
        }

        private static ITelemetryService getService() {
            return ServiceManager.getService(ITelemetryService.class);
        }
    }
}
