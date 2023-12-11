package com.redhat.devtools.intellij.telemetry.core.service.segment;

import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.service.Environment;
import com.redhat.devtools.intellij.telemetry.core.service.IDE;
import com.redhat.devtools.intellij.telemetry.core.service.UserId;

import static com.redhat.devtools.intellij.telemetry.core.IMessageBroker.*;

public class SegmentBrokerFactory implements IMessageBrokerFactory {

    @Override
    public IMessageBroker create(boolean isDebug, ClassLoader classLoader) {
        Environment environment = createEnvironment(classLoader);
        SegmentConfiguration configuration = new SegmentConfiguration(classLoader);
        return new SegmentBroker(
                isDebug,
                UserId.INSTANCE.get(),
                environment,
                configuration);
    }

    private static Environment createEnvironment(ClassLoader classLoader) {
        IDE ide = new IDE.Factory()
                .create()
                .setJavaVersion();
        return new Environment.Builder()
                .ide(ide)
                .plugin(classLoader)
                .build();
    }
}