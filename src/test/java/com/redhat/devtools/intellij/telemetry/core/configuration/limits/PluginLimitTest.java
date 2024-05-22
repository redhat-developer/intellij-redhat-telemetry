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

import com.redhat.devtools.intellij.telemetry.core.service.Event;
import com.redhat.devtools.intellij.telemetry.core.service.UserId;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.event;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.eventNameFilterFake;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.eventNameFilterFakeMatchingWithRatio;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.userId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PluginLimitTest {

    @Test
    public void canSend_should_return_false_if_enabled_is_OFF() {
        // given
        PluginLimits limits = new PluginLimits(
                "yoda",
                Enabled.OFF,
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                null); // no ratio check
        // when
        boolean canSend = limits.canSend(null);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_should_return_false_if_enabled_is_null() {
        // given
        PluginLimits limits = new PluginLimits(
                "yoda",
                null, // null enabled
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                null); // no ratio check;
        // when
        boolean canSend = limits.canSend(null);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_should_return_true_if_enabled_is_CRASH_and_event_has_error() {
        // given
        PluginLimits limits = new PluginLimits(
                "yoda",
                Enabled.CRASH, // only crash
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                null); // no ratio check
        Event error = event(
                Map.of("error", "anakin turned to the dark side")); // error
        // when
        boolean canSend = limits.canSend(error);
        // then
        assertThat(canSend).isTrue();
    }

    @Test
    public void canSend_should_return_false_if_enabled_is_CRASH_and_event_has_no_error() {
        // given
        PluginLimits limits = new PluginLimits(
                "yoda",
                Enabled.CRASH, // only crash
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                null); // no ratio check
        Event error = event(); // no error
        // when
        boolean canSend = limits.canSend(error);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_should_return_true_if_enabled_is_ERROR_and_event_has_error() {
        // given
        PluginLimits limits = new PluginLimits(
                "yoda",
                Enabled.ERROR, // only errors
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                null); // no ratio check
        Event error = event(
                Map.of("error", "anakin turned to the dark side")); // error
        // when
        boolean canSend = limits.canSend(error);
        // then
        assertThat(canSend).isTrue();
    }

    @Test
    public void canSend_should_return_false_if_enabled_is_ERROR_and_event_has_no_error() {
        // given
        PluginLimits limits = new PluginLimits(
                "yoda",
                Enabled.ERROR, // only errors
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                null); // no ratio check
        Event event = event(); // no error
        // when
        boolean canSend = limits.canSend(event);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_should_return_true_if_event_is_matched_by_inclusion_filter_with_ratio() {
        // given
        UserId userId = userId(1f);
        PluginLimits limits = new PluginLimits(
                "yoda is included",
                Enabled.ALL, // all enabled
                -1, // ignore
                1f, // ignore
                List.of(
                        // matching & ratio
                        eventNameFilterFake(true,true, false)
                ),
                Collections.emptyList(),
                userId);
        Event event = event(); // no error
        // when
        boolean canSend = limits.canSend(event);
        // then
        assertThat(canSend).isTrue();
    }

    @Test
    public void canSend_should_return_false_if_event_is_matched_by_exclusion_filter_with_ratio() {
        // given
        UserId userId = userId(1f);
        PluginLimits limits = new PluginLimits(
                "yoda is excluded",
                Enabled.ALL, // all enabled
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                List.of(
                        eventNameFilterFake(true,false, true)
                ),
                userId);
        Event event = event();
        // when
        boolean canSend = limits.canSend(event);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_should_return_false_if_event_is_matched_by_inclusion_and_exclusion_filter() {
        // given
        UserId userId = userId(1f);
        PluginLimits limits = new PluginLimits(
                "yoda cannot send",
                Enabled.ALL, // all enabled
                -1, // ignore
                1f, // ignore
                List.of(
                        eventNameFilterFake(true,true, false)
                ),
                List.of(
                        eventNameFilterFake(true,false, true)
                ),
                userId);
        Event event = event();
        // when
        boolean canSend = limits.canSend(event);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void isIncluded_should_return_true_if_there_is_no_include_filter() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event);
        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void isIncluded_should_return_true_if_there_is_no_matching_include_filter() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                List.of(
                        // is NOT matching
                        eventNameFilterFake(false,true, false)
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event);
        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void isIncluded_should_return_true_if_event_is_matching_filter_and_is_included() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                List.of(
                        // is matching & is excluded in ratio
                        eventNameFilterFake(true,true, false)
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event);
        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void isIncluded_should_return_false_if_event_is_matching_filter_but_isnt_included() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                List.of(
                        // is matching & is NOT included in ratio
                        eventNameFilterFake(true,false, false)
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event);
        // then
        assertThat(isIncluded).isFalse();
    }

    @Test
    public void isExcluded_should_return_false_if_there_is_no_filter() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isExcluded = limits.isExcluded(event);
        // then
        assertThat(isExcluded).isFalse();
    }

    @Test
    public void isExcluded_should_return_false_if_there_is_no_matching_filter() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                List.of(
                        // is NOT matching
                        eventNameFilterFake(false,true, true)
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isExcluded = limits.isExcluded(event);
        // then
        assertThat(isExcluded).isFalse();
    }

    @Test
    public void isExcluded_should_return_true_if_event_is_matching_filter_and_is_excluded_in_ratio() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                List.of(
                        // is matching & is excluded in ratio
                        eventNameFilterFake(true,true, true)
                ),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isExcluded = limits.isExcluded(event);
        // then
        assertThat(isExcluded).isTrue();
    }

    @Test
    public void isExcluded_should_return_false_if_event_is_matching_filter_and_is_NOT_excluded_in_ratio() {
        // given
        PluginLimits limits = new PluginLimits(
                null, // ignore
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ignore
                Collections.emptyList(),
                List.of(
                        // is matching & is NOT excluded in ratio
                        eventNameFilterFake(true,true, false)
                ),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isExcluded = limits.isExcluded(event);
        // then
        assertThat(isExcluded).isFalse();
    }

}
