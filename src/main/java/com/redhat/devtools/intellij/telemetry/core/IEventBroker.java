package com.redhat.devtools.intellij.telemetry.core;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent;
import com.redhat.devtools.intellij.telemetry.core.service.TrackEvent;

import java.util.List;

public interface IEventBroker {

    void send(TrackEvent event);
    void dispose();
}
