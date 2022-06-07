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

import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.util.Directories;
import com.redhat.devtools.intellij.telemetry.core.util.FileUtils;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public class UserId {

    private static final Logger LOGGER = Logger.getInstance(UserId.class);

    public static final UserId INSTANCE = new UserId();
    private static final Path UUID_FILE = Directories.RED_HAT.resolve("anonymousId");

    private final Lazy<String> uuid = new Lazy<>(() -> loadOrCreate(UUID_FILE));

    private UserId() {}

    public String get() {
        return uuid.get();
    }

    private String loadOrCreate(Path file) {
        if (Files.exists(file)) {
            return load(file);
        } else {
            String uuid = UUID.randomUUID().toString();
            write(uuid, file);
            return uuid;
        }
    }

    private String load(Path uuidFile) {
        String uuid = null;
        try(Stream<String> lines = Files.lines(uuidFile)) {
            uuid = lines
                    .findAny()
                    .map(String::trim)
                    .orElse(null);
        } catch (IOException e) {
            LOGGER.warn("Could not read redhat anonymous UUID file at " + uuidFile.toAbsolutePath(), e);
        }
        return uuid;
    }

    private void write(String uuid, Path uuidFile) {
        try {
            FileUtils.createFileAndParent(uuidFile);
            FileUtils.write(uuid, uuidFile);
        } catch (IOException e) {
            LOGGER.warn("Could not write redhat anonymous UUID to file at " + UUID_FILE.toAbsolutePath(), e);
        }
    }
}
