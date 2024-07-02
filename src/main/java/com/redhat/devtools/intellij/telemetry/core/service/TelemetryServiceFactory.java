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
package com.redhat.devtools.intellij.telemetry.core.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.DumbAware;
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.configuration.limits.IEventLimits;

@Service
public final class TelemetryServiceFactory implements DumbAware {

    public TelemetryService create(TelemetryConfiguration configuration, IEventLimits limits, IMessageBroker broker) {
        return new TelemetryService(configuration, limits, broker);
    }
}
