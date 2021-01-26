/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core;

import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.service.SegmentProperties;
import com.segment.analytics.Analytics;

import java.util.concurrent.TimeUnit;

public class AnalyticsFactory {

    private static final Logger LOGGER = Logger.getInstance(AnalyticsFactory.class);

    public static final AnalyticsFactory INSTANCE = new AnalyticsFactory();

    private  static final int FLUSH_INTERVAL = 10000;

    private AnalyticsFactory() {
    }

    public Analytics create() {
        return create(SegmentProperties.INSTANCE.getWriteKey());
    }

    public Analytics create(String writeKey) {
        if (writeKey == null) {
            LOGGER.warn("Could not create Segment Analytics instance, missing writeKey");
            return null;
        }
        return Analytics.builder(writeKey)
                .flushQueueSize(1)
                .flushInterval(FLUSH_INTERVAL, TimeUnit.MILLISECONDS)
                .build();
    }
}
