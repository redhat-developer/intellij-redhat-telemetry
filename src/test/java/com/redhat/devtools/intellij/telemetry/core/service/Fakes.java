package com.redhat.devtools.intellij.telemetry.core.service;

import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;
import com.redhat.devtools.intellij.telemetry.core.service.Application;
import com.redhat.devtools.intellij.telemetry.core.service.Environment;
import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;

import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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

    public static TelemetryConfiguration telemetryConfiguration(boolean enabled) {
        TelemetryConfiguration configuration = mock(TelemetryConfiguration.class);
        Mode mode = enabled? Mode.NORMAL : Mode.DISABLED;
        doReturn(mode)
                .when(configuration).getMode();
        return configuration;
    }

    public static TelemetryState telemetryState(boolean enabled) {
        TelemetryState state = TelemetryState.INSTANCE;
        state.setEnabled(enabled);
        return state;
    }

    public static ISegmentConfiguration segmentConfiguration(String writeKey, String debugWriteKey) {
        ISegmentConfiguration configuration = mock(ISegmentConfiguration.class);
        doReturn(writeKey)
                .when(configuration).getSegmentKey();
        doReturn(debugWriteKey)
                .when(configuration).getSegmentDebugKey();
        return configuration;
    }
}
