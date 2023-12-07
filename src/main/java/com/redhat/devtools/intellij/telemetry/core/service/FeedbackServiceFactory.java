/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.service;

import com.intellij.openapi.project.DumbAware;
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentBroker;
import com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentConfiguration;

public class FeedbackServiceFactory implements DumbAware {

    public IService create(ClassLoader classLoader) {
        TelemetryConfiguration configuration = TelemetryConfiguration.getInstance();
        IMessageBroker broker = createSegmentBroker(configuration.isDebug(), classLoader);
        return new FeedbackService(broker);
    }

    private IMessageBroker createSegmentBroker(boolean isDebug, ClassLoader classLoader) {
        SegmentConfiguration brokerConfiguration = new SegmentConfiguration(classLoader);
        return new SegmentBroker(
                isDebug,
                UserId.INSTANCE.get(),
                null,
                brokerConfiguration);
    }

}
