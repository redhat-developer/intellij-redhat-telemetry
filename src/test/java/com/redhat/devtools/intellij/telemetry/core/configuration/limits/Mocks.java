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

import com.redhat.devtools.intellij.telemetry.core.configuration.limits.Filter;
import com.redhat.devtools.intellij.telemetry.core.service.Event;
import com.redhat.devtools.intellij.telemetry.core.service.UserId;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class Mocks {

    public static Event event(Map<String, String> properties) {
        return new Event(null, null, properties);
    }

    public static Event event() {
        return event(new HashMap<>());
    }

    public static UserId userId(float percentile) {
        UserId userId = mock(UserId.class);
        doReturn(percentile)
                .when(userId).getPercentile();
        return userId;
    }

    public static Filter.EventNameFilter eventNameFilterFake(final boolean isMatching, boolean isIncludedRatio, boolean isExcludedRatio) {
        return new Filter.EventNameFilter( null, -1f, null) {
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

    public static Filter.EventNameFilter eventNameFilterFakeMatchingWithRatio(float ratio) {
        return new Filter.EventNameFilter( null, ratio, null) {
            @Override
            public boolean isMatching(Event event) {
                return true;
            }
        };
    }
}
