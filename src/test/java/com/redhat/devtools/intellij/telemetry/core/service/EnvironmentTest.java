package com.redhat.devtools.intellij.telemetry.core.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.redhat.devtools.intellij.telemetry.core.service.Environment.*;

public class EnvironmentTest {

    @Test
    public void should_build_correct_environment() {
        Environment env = new Environment.Builder()
                .application(Mockito.mock(Application.class))
                .plugin(Mockito.mock(Application.class))
                .build();
    }

}
