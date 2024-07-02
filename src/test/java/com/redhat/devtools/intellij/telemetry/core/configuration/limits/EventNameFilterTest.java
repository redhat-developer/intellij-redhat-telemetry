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

import com.redhat.devtools.intellij.telemetry.core.configuration.limits.Filter.EventNameFilter;
import com.redhat.devtools.intellij.telemetry.core.service.Event;
import com.redhat.devtools.intellij.telemetry.core.service.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EventNameFilterTest {

    @Test
    public void isMatching_should_match_event_name() {
        // given
        Filter filter = new EventNameFilter("yoda", 0.42f, 42);
        Event event = new Event(Event.Type.USER, "yoda");
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isTrue();
    }

    @Test
    public void isMatching_should_NOT_match_event_name_that_is_different() {
        // given
        Filter filter = new EventNameFilter("yoda", 0.42f, 42);
        Event event = new Event(Event.Type.USER, "darthvader");
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isFalse();
    }

    @Test
    public void isMatching_should_match_event_name_when_pattern_is_wildcard() {
        // given
        Filter filter = new EventNameFilter("*", 0.42f, 42);
        Event event = new Event(Event.Type.USER, "skywalker");
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isTrue();
    }

    @Test
    public void isMatching_should_match_event_name_when_pattern_has_name_with_wildcards() {
        // given
        Filter filter = new EventNameFilter("*walk*", 0.42f, 42);
        Event event = new Event(Event.Type.USER, "skywalker");
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isTrue();
    }

    @Test
    public void isIncludedByRatio_returns_true_if_percentile_is_within_ratio() {
        // given
        Filter filter = new EventNameFilter("ignore", 0.1f, EventNameFilter.DAILY_LIMIT_UNSPECIFIED);
        // when
        boolean isWithin = filter.isIncludedByRatio(0.1f);
        // then
        assertThat(isWithin).isTrue();
    }

    @Test
    public void isIncludedByRatio_returns_false_if_percentile_is_NOT_within_ratio() {
        // given
        Filter filter = new EventNameFilter("ignore", 0.1f, EventNameFilter.DAILY_LIMIT_UNSPECIFIED);
        // when
        boolean isWithin = filter.isIncludedByRatio(0.2f);
        // then
        assertThat(isWithin).isFalse();
    }

    @Test
    public void isIncludedByRatio_returns_false_if_ratio_is_0() {
        // given
        Filter filter = new EventNameFilter("ignore", 0, EventNameFilter.DAILY_LIMIT_UNSPECIFIED);
        // when
        boolean isWithin = filter.isIncludedByRatio(0);
        // then
        assertThat(isWithin).isFalse();
    }

    @Test
    public void isWithinDailyLimit_returns_true_if_dailyLyimit_is_unspecified() {
        // given
        Filter filter = new EventNameFilter("ignore", -1, EventNameFilter.DAILY_LIMIT_UNSPECIFIED);
        // when
        boolean isWithin = filter.isWithinDailyLimit(Integer.MAX_VALUE);
        // then
        assertThat(isWithin).isTrue();
    }

    @Test
    public void isWithinDailyLimit_returns_true_if_dailyLimit_is_NOT_reached() {
        // given
        Filter filter = new EventNameFilter("ignore", -1, Integer.MAX_VALUE);
        // when
        boolean isWithin = filter.isWithinDailyLimit(0);
        // then
        assertThat(isWithin).isTrue();
    }

    @Test
    public void isWithinDailyLimit_returns_false_if_dailyLimit_is_reached() {
        // given
        Filter filter = new EventNameFilter("ignore", -1, 1);
        // when
        boolean isWithin = filter.isWithinDailyLimit(2);
        // then
        assertThat(isWithin).isFalse();
    }

}
