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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.event;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.eventNameFilterFakeWithRatio;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.pluginLimitsWithIncludesExcludesWithPercentile;
import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Mocks.userId;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginLimitsRatioTests {

    @ParameterizedTest
    @MethodSource("canSend_for_include_ratio_and_percentile")
    public void canSend_for_given_ratio_in_limits_and_user_percentile(float limitsRatio, float percentile, boolean shouldSend) {
        // given
        PluginLimits limits = new PluginLimits(
                "jedis",
                Enabled.ALL, // ignore
                -1, // ignore
                limitsRatio,
                Collections.emptyList(),
                Collections.emptyList(),
                userId(percentile));
        Event event = event();
        // when
        boolean canSend = limits.canSend(event, 0);
        // then
        assertThat(canSend).isEqualTo(shouldSend);
    }

    @ParameterizedTest
    @MethodSource("canSend_for_include_ratio_and_percentile")
    public void canSend_for_given_ratio_in_include_filter_and_user_percentile(float filterRatio, float percentile, boolean shouldSend) {
        // given
        PluginLimits limits = new PluginLimits(
                "jedis",
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ratio 100%
                List.of(
                        eventNameFilterFakeWithRatio(filterRatio)
                ),
                Collections.emptyList(),
                userId(percentile));
        Event event = event();
        // when
        boolean canSend = limits.canSend(event,0);
        // then
        assertThat(canSend).isEqualTo(shouldSend);
    }

    private static Stream<Arguments> canSend_for_include_ratio_and_percentile() {
        return Stream.of(
                Arguments.of(0f, 0f, false), // ratio: 0, percentile: .2 -> false
                Arguments.of(0f, .2f, false), // ratio: 0, percentile: .2 -> false
                Arguments.of(.1f, .1f, true), // ratio: .1, percentile: .1 -> true
                Arguments.of(.5f, .4f, true), // ratio: .5, percentile: .4 -> true
                Arguments.of(.5f, .5f, true), // ratio: .5, percentile: .5 -> true
                Arguments.of(.5f, .6f, false), // ratio: .5, percentile: .6 -> false
                Arguments.of(1f, .6f, true), // ratio: 1, percentile: .6 -> true
                Arguments.of(1f, 1f, true) // ratio: 1, percentile: 1 -> true
        );
    }

    @ParameterizedTest
    @MethodSource("canSend_for_exclude_ratio_and_percentile")
    public void canSend_for_given_ratio_in_exclude_filter_and_user_percentile(float filterRatio, float percentile, boolean shouldSend) {
        // given
        PluginLimits limits = pluginLimitsWithIncludesExcludesWithPercentile(
                Collections.emptyList(),
                List.of(
                        eventNameFilterFakeWithRatio(filterRatio)
                ),
                userId(percentile));
        Event event = event();
        // when
        boolean canSend = limits.canSend(event,0);
        // then
        assertThat(canSend).isEqualTo(shouldSend);
    }

    private static Stream<Arguments> canSend_for_exclude_ratio_and_percentile() {
        return Stream.of(
                Arguments.of(0f, 0f, true), // exclude ratio: 0, percentile: .2 -> true
                Arguments.of(0f, .2f, true), // exclude ratio: 0, percentile: .2 -> true
                Arguments.of(.1f, .1f, true), // exclude ratio: .1, percentile: .1 -> true
                Arguments.of(.5f, .4f, true), // exclude ratio: .5, percentile: .4 -> true
                Arguments.of(.5f, .5f, true), // exclude ratio: .5, percentile: .5 -> true
                Arguments.of(.5f, .6f, false), // exclude ratio: .5, percentile: .6 -> false
                Arguments.of(1f, .6f, false), // exclude ratio: 1, percentile: .6 -> false
                Arguments.of(1f, 1f, false) // exclude ratio: 1, percentile: 1 -> false
        );
    }
}
