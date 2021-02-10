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

package com.redhat.devtools.intellij.telemetry.core.preferences;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;

import java.io.IOException;

import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.*;

/**
 * Supports storing the application settings in a persistent way.
 * The {@link State} and {@link Storage} annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
public class TelemetryState {

    public static final TelemetryState INSTANCE = new TelemetryState();

    private final TelemetryConfiguration configuration;

    private TelemetryState() {
        this.configuration = TelemetryConfiguration.INSTANCE;
    }

    public void setEnabled(boolean enabled) {
        configuration.setMode(toMode(enabled));
    }

    private Mode toMode(boolean enabled) {
        if (enabled) {
            return Mode.NORMAL;
        } else {
            return Mode.DISABLED;
        }
    }

    public boolean isEnabled() {
        switch(configuration.getMode()) {
            case NORMAL:
            case DEBUG:
               return true;
            default:
                return false;
        }
    }

    public void save() throws IOException {
        configuration.save();
    }
}