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
package com.redhat.devtools.intellij.telemetry.core.service.segment;

import com.redhat.devtools.intellij.telemetry.core.service.Environment;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;
import com.segment.analytics.Analytics;

public class TestableSegmentBroker extends SegmentBroker {

    public TestableSegmentBroker(boolean isDebug, String anonymousId, Environment environment, ISegmentConfiguration configuration, Analytics analytics) {
        super(isDebug, anonymousId, environment, configuration);
        this.analytics = new Lazy(() -> analytics);
    }
}
