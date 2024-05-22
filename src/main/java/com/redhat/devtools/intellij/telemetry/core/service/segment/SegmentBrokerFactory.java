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
package com.redhat.devtools.intellij.telemetry.core.service.segment;

import com.intellij.openapi.extensions.PluginDescriptor;
import com.redhat.devtools.intellij.telemetry.core.IMessageBroker;
import com.redhat.devtools.intellij.telemetry.core.service.Environment;
import com.redhat.devtools.intellij.telemetry.core.service.IDE;
import com.redhat.devtools.intellij.telemetry.core.service.UserId;

import static com.redhat.devtools.intellij.telemetry.core.IMessageBroker.*;

public class SegmentBrokerFactory implements IMessageBrokerFactory {

    @Override
    public IMessageBroker create(boolean isDebug, Environment environment, PluginDescriptor descriptor) {
        SegmentConfiguration configuration = new SegmentConfiguration(descriptor.getPluginClassLoader());
        return new SegmentBroker(
                isDebug,
                UserId.INSTANCE.get(),
                environment,
                configuration);
    }
}