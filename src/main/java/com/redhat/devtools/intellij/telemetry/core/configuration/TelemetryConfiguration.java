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
import java.util.Properties;

import static com.redhat.devtools.intellij.telemetry.core.configuration.ConfigurationConstants.*;

public class TelemetryConfiguration extends AbstractConfiguration implements ISegmentConfiguration {

    public static final TelemetryConfiguration INSTANCE = new TelemetryConfiguration();

    private static final FileConfiguration GLOBAL_FILE = new FileConfiguration(Paths.get(
            System.getProperty("user.home"),
            ".redhat",
            "com.redhat.devtools.intellij.telemetry"));

    public enum Mode {
        NORMAL, TEST, DISABLED, UNKNOWN;

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

    private TelemetryConfiguration() {
        super(new SystemProperties(
                new ConsumerClasspathFile(Paths.get("/segment.properties"),
                        GLOBAL_FILE)));
    }

    @Override
    public void setSegmentKey(String key) {
        put(KEY_SEGMENT_WRITE, key);
    }

    @Override
    public String getSegmentKey() {
        return get(KEY_SEGMENT_WRITE);
    }

    @Override
    public void setSegmentDebugKey(String key) {
        put(KEY_SEGMENT_DEBUG_WRITE, key);
    }

    @Override
    public String getSegmentDebugKey() {
        return get(KEY_SEGMENT_DEBUG_WRITE);
    }

    public void setMode(Mode mode) {
        put(KEY_MODE, mode.toString());
    }

    public Mode getMode() {
        return Mode.safeValueOf(get(KEY_MODE));
    }

    public boolean isEnabled() {
        return getMode() == Mode.NORMAL
                || getMode() != Mode.TEST;
    }

    public boolean isConfigured() {
        return getMode() != Mode.UNKNOWN;
    }

    public void save() throws IOException {
        GLOBAL_FILE.save();
    }

    @Override
    protected Properties loadProperties(IConfiguration parent) {
        return getParent().getProperties();
    }

    public void put(String key, String value) {
        GLOBAL_FILE.getProperties().put(key, value);
    }

}
