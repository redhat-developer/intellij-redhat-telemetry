package com.redhat.devtools.intellij.telemetry.core.service;

import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;
import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Fakes {

    public static Environment environment(
            String extensionName,
            String extensionVersion,
            String applicationName,
            String applicationVersion) {
        return new Environment.Builder()
                .application(new Application(applicationName, applicationVersion))
                .plugin(new Application(extensionName, extensionVersion))
                .build();
    }

    public static TelemetryConfiguration telemetryConfiguration(boolean enabled, boolean configured) {
        TelemetryConfiguration configuration = mock(TelemetryConfiguration.class);
        doReturn(enabled)
                .when(configuration).isEnabled();
        doReturn(configured)
                .when(configuration).isConfigured();
        return configuration;
    }

    public static ISegmentConfiguration segmentConfiguration(boolean debug, String writeKey, String debugWriteKey) {
        ISegmentConfiguration configuration = mock(ISegmentConfiguration.class);
        when(configuration.getSegmentNormalKey())
                .thenAnswer((InvocationOnMock invocation) -> {
                        if (debug) {
                            return debugWriteKey;
                        } else {
                            return writeKey;
                        }
                });
        return configuration;
    }
}
