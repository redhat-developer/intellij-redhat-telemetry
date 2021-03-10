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

import static com.redhat.devtools.intellij.telemetry.core.util.ConfigurationUtils.createPropertyFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SaveableFileConfigurationTest {

    private SaveableFileConfiguration config;
    private Path path;

    private static final Pair<String, String> property1 = new Pair<>("luke", "jedy");
    private static final Pair<String, String> property2 = new Pair<>("anakin", "sith");

    @BeforeEach
    public void beforeEach() throws IOException {
        this.path = Paths.get(System.getProperty("java.io.tmpdir"), getClass().getSimpleName() + ".properties");
        createPropertyFile(path, property1);
        this.config = new SaveableFileConfiguration(path);
    }

    @Test
    public void save_creates_property_file_if_it_doesnt_exist() throws IOException {
        // given
        Files.delete(path);
        assertThat(Files.exists(path)).isFalse();
        // when
        config.save();
        // then
        assertThat(Files.exists(path)).isTrue();
    }

    @Test
    public void save_saves_additional_properties() throws IOException {
        // given
        assertThat(config.get(property2.first)).isNull();
        config.put(property2.first, property2.second);
        // when
        config.save();
        // then
        this.config = new SaveableFileConfiguration(path);
        assertThat(config.get(property2.first)).isEqualTo(property2.second);
    }
}
