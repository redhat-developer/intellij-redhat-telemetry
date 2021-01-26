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
package com.redhat.devtools.intellij.telemetry.ui.preferences;

import com.intellij.openapi.options.Configurable;
import com.redhat.devtools.intellij.telemetry.core.preferences.TelemetryState;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * Controller for telemetry settings.
 */
public class TelemetryConfigurable implements Configurable {

    private TelemetryComponent component;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "SDK: Application Settings Example";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        this.component = new TelemetryComponent();
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        TelemetryState state = TelemetryState.getInstance();
        boolean modified = false;
        modified |= (component.isEnabled() != state.enabled);
        return modified;
    }

    @Override
    public void apply() {
        TelemetryState state = TelemetryState.getInstance();
        state.enabled = component.isEnabled();
    }

    @Override
    public void reset() {
        TelemetryState state = TelemetryState.getInstance();
        component.setEnabled(state.enabled);
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

}