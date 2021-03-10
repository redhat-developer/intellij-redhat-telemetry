package com.redhat.devtools.intellij.telemetry.core.service;

import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Fakes {

    public static Environment environment(
            String extensionName,
            String extensionVersion,
            String applicationName,
            String applicationVersion,
            String platform_name,
            String platform_distribution,
            String platform_version,
            String locale,
            String timezone) {
        return new Environment.Builder()
                .application(new Application(applicationName, applicationVersion))
                .locale(locale)
                .timezone(timezone)
                .platform(new Platform(platform_name, platform_distribution, platform_version))
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

    public static ISegmentConfiguration segmentConfiguration(String normalWriteKey, String debugWriteKey) {
        ISegmentConfiguration configuration = mock(ISegmentConfiguration.class);
        when(configuration.getNormalWriteKey())
                .thenReturn(normalWriteKey);
        when(configuration.getDebugWriteKey())
                .thenReturn(debugWriteKey);
        return configuration;
    }
}
