package com.redhat.devtools.intellij.telemetry.core.service;

import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.ISegmentConfiguration;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Fakes {

    public static TelemetryConfiguration telemetryConfiguration(boolean enabled, boolean configured) {
        TelemetryConfiguration configuration = mock(TelemetryConfiguration.class);
        doReturn(enabled)
                .when(configuration).isEnabled();
        doReturn(configured)
                .when(configuration).isConfigured();
        return configuration;
    }

    public static Environment environment(
            String extensionName,
            String extensionVersion,
            String applicationName,
            String applicationVersion,
            String platform_name,
            String platform_distribution,
            String platform_version,
            String locale,
            String timezone,
            String country) {
        return new Environment.Builder()
                .ide(new IDE(applicationName, applicationVersion))
                .locale(locale)
                .timezone(timezone)
                .country(country)
                .platform(new Platform(platform_name, platform_distribution, platform_version))
                .plugin(new Plugin.Factory().create(extensionName, extensionVersion))
                .build();
    }

    public static ISegmentConfiguration segmentConfiguration(String normalWriteKey, String debugWriteKey) {
        ISegmentConfiguration configuration = mock(ISegmentConfiguration.class);
        when(configuration.getNormalWriteKey())
                .thenReturn(normalWriteKey);
        when(configuration.getDebugWriteKey())
                .thenReturn(debugWriteKey);
        when(configuration.getWriteKey(anyBoolean()))
                .thenAnswer((Answer<String>) invocation -> {
                    Boolean isDebug = invocation.getArgument(0);
                    if (isDebug) {
                        return debugWriteKey;
                    } else {
                        return normalWriteKey;
                    }
                });
        return configuration;
    }

    public static TelemetryConfiguration telemetryConfiguration(TelemetryConfiguration.Mode mode) {
        TelemetryConfiguration configuration = mock(TelemetryConfiguration.class);
        when(configuration.isEnabled())
                .thenReturn(mode != TelemetryConfiguration.Mode.DISABLED);
        when(configuration.isConfigured())
                .thenReturn(true);
        when(configuration.getMode())
                .thenReturn(mode);
        return configuration;
    }

}
