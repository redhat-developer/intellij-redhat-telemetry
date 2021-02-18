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

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.Telemetry;
import com.redhat.devtools.intellij.telemetry.ui.TelemetryNotifications;

public class OnPluginStarted implements ApplicationInitializedListener, StartupActivity {

    @Override
    public void componentsInitialized() {
        // identify user (not sent if telemetry is not accepted)
        Telemetry.builder(getClass().getClassLoader()).user().send();
    }

    @Override
    public void runActivity(Project project) {
        // ask user to consent to telemetry
        if (!TelemetryConfiguration.INSTANCE.isConfigured()) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> TelemetryNotifications.queryUserConsent());
        }
    }
}
