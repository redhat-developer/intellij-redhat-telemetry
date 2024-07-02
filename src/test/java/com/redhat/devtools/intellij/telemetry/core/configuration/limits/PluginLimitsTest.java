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
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.eventNameFilter;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.eventNameWithDailyLimit;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.eventProperty;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.pluginLimitsWithIncludesExcludes;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.pluginLimitsWithIncludesExcludesWithPercentile;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.userId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PluginLimitsTest {

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
        boolean canSend = limits.canSend(null, Integer.MAX_VALUE);
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
        boolean canSend = limits.canSend(null, Integer.MAX_VALUE);
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
        boolean canSend = limits.canSend(error, 0);
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
        boolean canSend = limits.canSend(error, 0);
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
        boolean canSend = limits.canSend(error, 0);
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
        boolean canSend = limits.canSend(event, 0);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_should_return_true_if_event_is_matched_by_inclusion_filter_with_ratio() {
        // given
        UserId userId = userId(1f);
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        // matching & ratio
                        eventNameFilter(true,true, false)
                ),
                Collections.emptyList(),
                userId);
        Event event = event(); // no error
        // when
        boolean canSend = limits.canSend(event, 0);
        // then
        assertThat(canSend).isTrue();
    }

    @Test
    public void canSend_should_return_false_if_event_is_matched_by_exclusion_filter_with_ratio() {
        // given
        UserId userId = userId(1f);
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                Collections.emptyList(),
                List.of(
                        eventNameFilter(true,false, true)
                ),
                userId);
        Event event = event();
        // when
        boolean canSend = limits.canSend(event, 0);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_should_return_false_if_event_is_matched_by_inclusion_and_exclusion_filter() {
        // given
        UserId userId = userId(1f);
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        eventNameFilter(true,true, false)
                ),
                List.of(
                        eventNameFilter(true,false, true)
                ),
                userId);
        Event event = event();
        // when
        boolean canSend = limits.canSend(event, 0);
        // then
        assertThat(canSend).isFalse();
    }

    @Test
    public void canSend_returns_true_if_dailyLimit_is_not_reached() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludes(
                List.of(eventNameWithDailyLimit(Integer.MAX_VALUE)),
                Collections.emptyList());
        Event event = event();
        // when
        boolean canSend = limits.canSend(event,0);
        // then
        assertThat(canSend).isEqualTo(true);
    }

    @Test
    public void canSend_returns_false_if_dailyLimit_is_reached() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludes(
                List.of(eventNameWithDailyLimit(1)),
                Collections.emptyList());
        Event event = event();
        // when
        boolean canSend = limits.canSend(event,1);
        // then
        assertThat(canSend).isEqualTo(false);
    }

    @Test
    public void canSend_returns_true_for_property_filter_event_though_dailyLimit_is_reached() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludes(
                List.of(eventProperty()),
                Collections.emptyList());
        Event event = event();
        // when
        boolean canSend = limits.canSend(event,Integer.MAX_VALUE);
        // then
        assertThat(canSend).isEqualTo(true);
    }

    @Test
    public void isIncluded_should_return_true_if_there_is_no_include_filter() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                Collections.emptyList(),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event, 0);
        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void isIncluded_should_return_true_if_there_is_no_matching_include_filter() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        // is NOT matching
                        eventNameFilter(false,true, false)
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event, 0);
        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void isIncluded_should_return_true_if_event_is_matching_filter_and_is_included() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        // is matching & is excluded in ratio
                        eventNameFilter(true,true, false)
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event, 0);
        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void isIncluded_should_return_false_if_event_is_matching_filter_but_isnt_included() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        // is matching & is NOT included in ratio
                        eventNameFilter(true,false, false)
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event, 0);
        // then
        assertThat(isIncluded).isFalse();
    }

    @Test
    public void isIncluded_should_return_true_if_event_is_within_daily_limit() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        // is matching & is NOT included in ratio
                        eventNameFilter(true,true, false, 1) // max 1 daily
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event, 0); // 0 events so far
        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void isIncluded_should_return_false_if_daily_limit_reached_already() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        // is matching & is NOT included in ratio
                        eventNameFilter(true,true, false, 1) // max 1 daily
                ),
                Collections.emptyList(),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isIncluded = limits.isIncluded(event, 1); // 1 event so far, no room left
        // then
        assertThat(isIncluded).isFalse();
    }

    @Test
    public void isExcluded_should_return_false_if_there_is_no_filter() {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
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
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                List.of(
                        // is NOT matching
                        eventNameFilter(false,true, true)
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
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                Collections.emptyList(),
                List.of(
                        // is matching & is excluded in ratio
                        eventNameFilter(true,true, true)
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
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                Collections.emptyList(),
                List.of(
                        // is matching & is NOT excluded in ratio
                        eventNameFilter(true,true, false)
                ),
                mock(UserId.class));
        Event event = event();
        // when
        boolean isExcluded = limits.isExcluded(event);
        // then
        assertThat(isExcluded).isFalse();
    }

}
