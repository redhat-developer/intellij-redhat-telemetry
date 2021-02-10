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

import com.intellij.openapi.diagnostic.Logger;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Properties;

public class FileConfiguration extends AbstractConfiguration {

    private static final Logger LOGGER = Logger.getInstance(FileConfiguration.class);

    private final Path file;

    public FileConfiguration(Path file) {
        super();
        this.file = file;
    }

    @Override
    protected Properties loadProperties() {
        Properties properties = new Properties();
        if (file == null) {
            return properties;
        }

        try (InputStream in = createFileInputStream(file)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not load properties file " + file);
        }
        return properties;
    }

    protected InputStream createFileInputStream(Path path) throws IOException {
        if (path == null) {
            return null;
        }
        ensureExists(path);
        return new FileInputStream(path.toFile());
    }

    private boolean ensureExists(Path file) throws IOException {
        if (file == null) {
            return false;
        }
        if (file.toFile().exists()) {
            return true;
        }
        return file.toFile().createNewFile();
    }


    public void save() throws IOException {
        if (file == null) {
            return;
        }
        try (Writer writer = new FileWriter(file.toFile());) {
            getProperties().store(writer, "");
        }
    }
}
