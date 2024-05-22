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
import com.redhat.devtools.intellij.telemetry.core.util.BasicGlobPattern;

public interface Filter {

    boolean isMatching(Event event);

    boolean isIncludedByRatio(float percentile);

    boolean isExcludedByRatio(float percentile);

    boolean isWithinDailyLimit(int total);

    class EventPropertyFilter implements Filter {
        private final String name;
        private final BasicGlobPattern glob;

        EventPropertyFilter(String name, String valueGlob) {
            this.name = name;
            this.glob = BasicGlobPattern.compile(valueGlob);
        }

        @Override
        public boolean isMatching(Event event) {
            String value = event.getProperties().get(name);
            return glob.matches(value);
        }

        @Override
        public boolean isIncludedByRatio(float percentile) {
            return true;
        }

        @Override
        public boolean isExcludedByRatio(float percentile) {
            return false;
        }

        @Override
        public boolean isWithinDailyLimit(int total) {
            return true;
        }
    }

    class EventNameFilter implements Filter {

        static final int DAILY_LIMIT_UNSPECIFIED = -1;

        private final BasicGlobPattern name;
        private final float ratio;
        private final int dailyLimit;

        EventNameFilter(String name, float ratio, int dailyLimit) {
            this.name = BasicGlobPattern.compile(name);
            this.ratio = ratio;
            this.dailyLimit = dailyLimit;
        }

        public float getRatio() {
            return ratio;
        }

        public int getDailyLimit() {
            return dailyLimit;
        }

        @Override
        public boolean isMatching(Event event) {
            return name.matches(event.getName());
        }

        @Override
        public boolean isIncludedByRatio(float percentile) {
            return ratio != 0
                    && percentile <= ratio;
        }

        @Override
        public boolean isExcludedByRatio(float percentile) {
            return 1 - ratio < percentile;
        }

        @Override
        public boolean isWithinDailyLimit(int total) {
            if (dailyLimit == DAILY_LIMIT_UNSPECIFIED) {
                return true;
            } else {
                return total < dailyLimit; // at least 1 more to go
            }
        }
    }
}