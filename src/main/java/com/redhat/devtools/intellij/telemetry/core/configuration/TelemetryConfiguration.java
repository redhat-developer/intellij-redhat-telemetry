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
package com.redhat.devtools.intellij.telemetry.core.configuration;

import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.redhat.devtools.intellij.telemetry.core.configuration.ConfigurationConstants.*;

public class TelemetryConfiguration extends CompositeConfiguration {

    public static final TelemetryConfiguration INSTANCE = new TelemetryConfiguration();

    private TelemetryConfiguration() {
    }

    public static final FileConfiguration GLOBAL_FILE = new FileConfiguration(Paths.get(
            System.getProperty("user.home"),
            ".redhat",
            "com.redhat.devtools.intellij.telemetry"));

    public void setMode(Mode mode) {
        put(KEY_MODE, mode.toString());
    }

    public Mode getMode() {
        return Mode.safeValueOf(get(KEY_MODE));
    }

    public boolean isEnabled() {
        switch(getMode()) {
            case NORMAL:
            case DEBUG:
                return true;
            default:
                return false;
        }
    }

    public boolean isDebug() {
        return getMode() == Mode.DEBUG;
    }

    public boolean isConfigured() {
        return getMode() != Mode.UNKNOWN;
    }

    @Override
    public void put(String key, String value) {
        GLOBAL_FILE.getProperties().put(key, value);
    }

    public void save() throws IOException {
        GLOBAL_FILE.save();
    }

    @Override
    protected List<IConfiguration> getConfigurations() {
        return Arrays.asList(
                new SystemProperties(),
                GLOBAL_FILE);
    }

    public enum Mode {
        NORMAL, DEBUG, DISABLED, UNKNOWN;

        public static Mode safeValueOf(String value) {
            try {
                if (value == null) {
                    return UNKNOWN;
                }
                return Mode.valueOf(value);
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

}
