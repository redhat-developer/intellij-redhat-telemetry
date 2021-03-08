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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EnvironmentTest {

    @Test
    public void should_build_correct_environment() {
        Environment env = new Environment.Builder()
                .application(Mockito.mock(Application.class))
                .plugin(Mockito.mock(Application.class))
                .build();
    }

}
