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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Filter.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class Mocks {

    public static Event event() {
        return event(new HashMap<>());
    }

    public static Event event(Map<String, String> properties) {
        return new Event(null, null, properties);
    }

    public static UserId userId(float percentile) {
        UserId userId = mock(UserId.class);
        doReturn(percentile)
                .when(userId).getPercentile();
        return userId;
    }

    public static PluginLimits pluginLimitsWithIncludesExcludes(List<Filter> includes, List<Filter> excludes) {
        return pluginLimitsWithIncludesExcludesWithPercentile(
                includes,
                excludes,
                userId(0));
    }

    public static PluginLimits pluginLimitsWithIncludesExcludesWithPercentile(List<Filter> includes, List<Filter> excludes, UserId userId) {
        return new PluginLimits(
                "jedis",
                Enabled.ALL, // ignore
                -1, // ignore
                1f, // ratio 100%
                includes,
                excludes,
                userId);
    }

    public static EventNameFilter eventNameFilter(final boolean isMatching, boolean isIncludedRatio, boolean isExcludedRatio) {
        return eventNameFilter(isMatching, isIncludedRatio, isExcludedRatio, Integer.MAX_VALUE); // no daily limit
    }

    public static EventNameFilter eventNameFilter(final boolean isMatching, boolean isIncludedRatio, boolean isExcludedRatio, int dailyLimit) {
        return new EventNameFilter( null, -1f, dailyLimit) {
            @Override
            public boolean isMatching(Event event) {
                return isMatching;
            }

            @Override
            public boolean isExcludedByRatio(float percentile) {
                return isExcludedRatio;
            }

            @Override
            public boolean isIncludedByRatio(float percentile) {
                return isIncludedRatio;
            }
        };
    }

    public static EventNameFilter eventNameFilterFakeWithRatio(float ratio) {
        return new EventNameFilter( null, ratio, Integer.MAX_VALUE) {
            @Override
            public boolean isMatching(Event event) {
                return true;
            }
        };
    }

    public static EventNameFilter eventNameWithDailyLimit(int dailyLimit) {
        return new EventNameFilter( null, 1, dailyLimit) {
            @Override
            public boolean isMatching(Event event) {
                return true;
            }
        };
    }

    public static EventPropertyFilter eventProperty() {
        return new EventPropertyFilter( null, null) {
            @Override
            public boolean isMatching(Event event) {
                return true;
            }
        };
    }

}
