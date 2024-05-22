/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.configuration.limits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.MessageLimits.PluginLimitsFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class MessageLimitsTest {

    private MessageLimits limits;

    @BeforeEach
    public void before() {
        this.limits = new MessageLimits(
                mock(PluginLimitsFactory.class),
                mock(Configurations.class));
    }

    @Test
    public void getDefaultLimits_should_return_null_if_there_are_no_limits() {
        // given
        // when
        PluginLimits pluginLimits = limits.getDefaultLimits(null);
        // then
        assertThat(pluginLimits).isNull();
    }

    @Test
    public void getDefaultLimits_should_return_null_if_there_is_no_default_limit() {
        // given
        List<PluginLimits> noDefault = List.of(
                mock(PluginLimits.class)
        );
        // when
        PluginLimits pluginLimits = limits.getDefaultLimits(noDefault);
        // then
        assertThat(pluginLimits).isNull();
    }

    @Test
    public void getDefaultLimits_should_return_default_limits_if_it_exists() {
        // given
        PluginLimits defaultLimits = mock(PluginLimits.class);
        doReturn(true)
                .when(defaultLimits).isDefault();
        List<PluginLimits> containsDefault = List.of(
                mock(PluginLimits.class),
                defaultLimits,
                mock(PluginLimits.class)
        );
        // when
        PluginLimits found = limits.getDefaultLimits(containsDefault);
        // then
        assertThat(found).isEqualTo(defaultLimits);
    }

    @Test
    public void get_should_return_empty_list_of_limits_if_deserialization_throws() throws IOException {
        // given
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        doThrow(new IOException())
                .when(factory).create(any());
        Configurations configurations = mock(Configurations.class);
        MessageLimits limits = new MessageLimits(factory, configurations);
        // when
        List<PluginLimits> pluginLimits = limits.get();
        // then
        assertThat(pluginLimits).isEmpty();
    }

    @Test
    public void needsRefresh_should_return_true_if_there_is_no_local_file() {
        // given
        // when
        boolean stale = limits.needsRefresh(null, null); // no modified date aka no file
        // then
        assertThat(stale).isTrue();
    }

    @Test
    public void needsRefresh_should_return_false_if_refresh_period_is_not_exceeded() {
        // given
        FileTime created1hoursAgo = FileTime.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Duration refreshAfter2hours = Duration.of(2, ChronoUnit.HOURS);
        // when
        boolean needsRefresh = limits.needsRefresh(refreshAfter2hours, created1hoursAgo);
        // then
        assertThat(needsRefresh).isFalse();
    }

    @Test
    public void needsRefresh_should_return_true_if_refresh_period_is_exceeded() {
        // given
        FileTime created2hoursAgo = FileTime.from(Instant.now().minus(2, ChronoUnit.HOURS));
        Duration refreshAfter1hours = Duration.of(1, ChronoUnit.HOURS);
        // when
        boolean needsRefresh = limits.needsRefresh(refreshAfter1hours, created2hoursAgo);
        // then
        assertThat(needsRefresh).isTrue();
    }

}
