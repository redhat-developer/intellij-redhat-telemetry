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

import com.intellij.openapi.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.redhat.devtools.intellij.telemetry.core.util.ConfigurationUtils.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FileConfigurationTest {

    private FileConfiguration config;
    private Path path;

    private static final Pair<String, String> property1 = new Pair<>("luke", "jedy");

    @BeforeEach
    public void beforeEach() throws IOException {
        this.path = Paths.get(System.getProperty("java.io.tmpdir"), getClass().getSimpleName() + ".properties");
        createPropertyFile(path, property1);
        this.config = new FileConfiguration(path);
    }

    @Test
    public void get_loads_property_file() throws IOException {
        // given
        // when
        String value = config.get(property1.first);
        // then
        assertThat(value).isEqualTo(property1.second);
    }

    @Test
    public void get_returns_null_if_no_file_exists() throws IOException {
        // given
        Files.delete(path);
        // when
        String value = config.get(property1.first);
        // then
        assertThat(value).isNull();
    }

}
