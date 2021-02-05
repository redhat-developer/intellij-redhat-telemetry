package com.redhat.devtools.intellij.telemetry.core.service;

import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;

public class Fakes {

    public static Environment environment(
            String extensionName,
            String extensionVersion,
            String applicationName,
            String applicationVersion) {
        return Environment.builder()
                .plugin(new Application(extensionName, extensionVersion))
                .application(new Application(applicationName, applicationVersion))
                .build();
    }

    public static TelemetryState telemetryState(boolean enabled) {
        TelemetryState state = new TelemetryState();
        state.setEnabled(enabled);
        return state;
    }
}
