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

    }

    class EventNameFilter implements Filter {
        private final BasicGlobPattern name;
        private final float ratio;
        private final String dailyLimit;

        EventNameFilter(String name, float ratio, String dailyLimit) {
            this.name = BasicGlobPattern.compile(name);
            this.ratio = ratio;
            this.dailyLimit = dailyLimit;
        }

        public float getRatio() {
            return ratio;
        }

        public String getDailyLimit() {
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
    }
}