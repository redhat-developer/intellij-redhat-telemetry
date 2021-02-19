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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class TelemetryConfiguration extends CompositeConfiguration {

    public static final String KEY_MODE = "mode";

    public static final TelemetryConfiguration INSTANCE = new TelemetryConfiguration();

    public static final FileConfiguration GLOBAL_FILE = new FileConfiguration(Paths.get(
            System.getProperty("user.home"),
            ".redhat",
            "com.redhat.devtools.intellij.telemetry"));
    private final TelemetryConfigurationNotifier notifier;

    private TelemetryConfiguration() {
        this(ApplicationManager.getApplication().getMessageBus());
    }

    public TelemetryConfiguration(final MessageBus messageBus) {
        this.notifier = messageBus.syncPublisher(TelemetryConfigurationNotifier.CONFIGURATION_CHANGED);
    }

    public void setMode(Mode mode) {
        put(KEY_MODE, mode.toString());
    }

    public Mode getMode() {
        return Mode.safeValueOf(get(KEY_MODE));
    }

    public boolean isEnabled() {
        return Mode.toEnabledBoolean(getMode());
    }

    public void setEnabled(boolean enabled) {
        setMode(Mode.toEnabledMode(enabled));
    }

    public boolean isDebug() {
        return getMode() == Mode.DEBUG;
    }

    public boolean isConfigured() {
        return getMode() != Mode.UNKNOWN;
    }

    @Override
    public void put(String key, String value) {
        GLOBAL_FILE.properties.get().put(key, value);
        notifier.configurationChanged(key, value);
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

        public static Mode toEnabledMode(boolean enabled) {
            if (enabled) {
                return Mode.NORMAL;
            } else {
                return Mode.DISABLED;
            }
        }

        public static boolean toEnabledBoolean(String value) {
            return toEnabledBoolean(safeValueOf(value));
        }

        public static boolean toEnabledBoolean(Mode mode) {
            switch(mode) {
                case NORMAL:
                case DEBUG:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static interface TelemetryConfigurationNotifier {
        Topic<TelemetryConfigurationNotifier> CONFIGURATION_CHANGED =
                Topic.create("Telemetry Configuration Changed", TelemetryConfigurationNotifier.class);

        void configurationChanged(String property, String value);
    }

}
