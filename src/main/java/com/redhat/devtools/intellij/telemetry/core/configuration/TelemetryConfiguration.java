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

import java.nio.file.Paths;
import java.util.Properties;

import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConstants.*;

public class TelemetryConfiguration extends AbstractConfiguration {

    private static final IConfiguration GLOBAL_FILE = new FileConfiguration(Paths.get(
            System.getProperty("user.home"),
            ".redhat",
            "com.redhat.devtools.intellij.telemetry"));

    public enum Mode {
        NORMAL, TEST, DISABLED, UNKNOWN;

        public static Mode safeValueOf(String value) {
            try {
                return Mode.valueOf(value);
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    public TelemetryConfiguration() {
        super(new SystemProperties(
                new ConsumerClasspathFile(Paths.get("/segment.properties"),
                        GLOBAL_FILE)));
    }

    public void setSegmentKey(String key) {
        put(KEY_SEGMENT_WRITE, key);
    }

    public String getSegmentKey() {
        return (String) get(KEY_SEGMENT_WRITE);
    }

    public void setSegmentDebugKey(String key) {
        put(KEY_DEBUG_SEGMENT_WRITE, key);
    }

    public String getDebugSegmentKey() {
        return (String) get(KEY_DEBUG_SEGMENT_WRITE);
    }

    public void setMode(Mode mode) {
        put(KEY_MODE, mode.toString());
    }

    public Mode getMode() {
        return Mode.safeValueOf((String) get(KEY_MODE));
    }

    public IConfiguration getWritable() {
        return GLOBAL_FILE;
    }

    @Override
    protected Properties loadProperties(IConfiguration parent) {
        return getParent().getProperties();
    }
}
