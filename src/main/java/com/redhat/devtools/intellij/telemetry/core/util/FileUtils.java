/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.util;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    private static final String FILE_URL_PREFIX = "file:";

    private FileUtils() {
    }

    /**
     * Creates the file for the given path and the folder that contains it.
     * Does nothing if it any of those already exist.
     *
     * @param file the file to create
     *
     * @throws IOException if the file operation fails
     */
    public static void createFileAndParent(Path file) throws IOException {
        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    public static Path ensureExists(@NotNull Path path) throws IOException {
        FileUtils.createFileAndParent(path);
        return path;
    }

    public static void write(String content, Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.append(content);
        }
    }

    public static boolean isFileUrl(String url) {
        return !StringUtils.isEmpty(url)
            && url.startsWith(FILE_URL_PREFIX);
    }

    @Nullable
    public static Path getPathForFileUrl(String url) {
        if (!isFileUrl(url)) {
            return null;
        }
        try {
            URI uri = new URL(url).toURI();
            return new File(uri).toPath();
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

}
