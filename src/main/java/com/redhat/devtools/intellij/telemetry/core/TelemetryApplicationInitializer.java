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
import com.redhat.devtools.intellij.telemetry.core.service.Telemetry;

public class TelemetryApplicationInitializer implements ApplicationInitializedListener {
	@Override
	public void componentsInitialized() {
		Telemetry.builder(getClass().getClassLoader()).user().send();
	}
}
