package com.redhat.devtools.intellij.telemetry.core;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent;

public interface IEventBroker {

    void send(TelemetryEvent event);
    void dispose();
}
