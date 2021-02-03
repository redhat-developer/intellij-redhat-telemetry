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

package com.redhat.devtools.intellij.telemetry.core.preferences;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Supports storing the application settings in a persistent way.
 * The {@link State} and {@link Storage} annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
        name = "com.redhat.devtools.intellij.common.telemetry.service.TelemetryState",
        storages = {@Storage("TelemetrySettings.xml")}
)
public class TelemetryState implements PersistentStateComponent<TelemetryState> {

    public boolean enabled = false;

    private final TelemetryConfiguration configuration;

    public static TelemetryState getInstance() {
        return ServiceManager.getService(TelemetryState.class);
    }

    public TelemetryState() {
        this.configuration = new TelemetryConfiguration();
    }

    @Nullable
    @Override
    public TelemetryState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TelemetryState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}