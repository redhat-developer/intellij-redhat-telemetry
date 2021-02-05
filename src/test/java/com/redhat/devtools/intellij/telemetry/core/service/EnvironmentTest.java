package com.redhat.devtools.intellij.telemetry.core.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.redhat.devtools.intellij.telemetry.core.service.Environment.*;

public class EnvironmentTest {

    @Test
    public void should_build_correct_environment() {
        Environment env = Environment.builder()
                .plugin(Mockito.mock(Application.class))
                .application(Mockito.mock(Application.class))
                .build();
    }

}
