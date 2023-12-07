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
package com.redhat.devtools.intellij.telemetry.core.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ClasspathConfigurationTest {

    private ClasspathConfiguration config;

    @BeforeEach
    void beforeEach() throws IOException {
        Path path = Paths.get("segment.properties");
        this.config = new ClasspathConfiguration(path);
    }

    @Test
    void get_loads_property_file() throws IOException {
        // given
        // when
        String value = config.get("writeKey");
        // then
        assertThat(value).isEqualTo("SEGPROP-normal");
    }
}
