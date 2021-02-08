package com.redhat.devtools.intellij.telemetry.core.service;

import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;
import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        doReturn(enabled)
                .when(configuration).isEnabled();
        return configuration;
    }

    public static TelemetryState telemetryState(boolean enabled) {
        TelemetryState state = TelemetryState.INSTANCE;
        state.setEnabled(enabled);
        return state;
    }

    public static ISegmentConfiguration segmentConfiguration(boolean debug, String writeKey, String debugWriteKey) {
        ISegmentConfiguration configuration = mock(ISegmentConfiguration.class);
        when(configuration.getSegmentKey())
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        if (debug) {
                            return debugWriteKey;
                        } else {
                            return writeKey;
                        }
                    }
                });
        return configuration;
    }
}
