package com.redhat.devtools.intellij.telemetry.core.service;

import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class UserId {

    private static final Logger LOGGER = Logger.getInstance(UserId.class);

    public static final UserId INSTANCE = new UserId();
    private static final Path REDHAT_DIRECTORY = Paths.get(System.getProperty("user.home"), ".redhat");
    private static final Path UUID_FILE = REDHAT_DIRECTORY.resolve("anonymousId");

    private String uuid;

    private UserId() {}

    public String get() {
        if (uuid == null) {
            if (Files.exists(UUID_FILE)) {
                this.uuid = load(UUID_FILE);
            } else {
                this.uuid = UUID.randomUUID().toString();
                write(uuid, REDHAT_DIRECTORY, UUID_FILE);
            }
        }
        return uuid;
    }

    private String load(Path uuidFile) {
        String uuid = null;
        try {
            uuid = Files.lines(uuidFile)
                    .findAny()
                    .map(String::trim)
                    .orElse(null);
        } catch (IOException e) {
            LOGGER.warn("Could not read redhat anonymous UUID file at " + uuidFile.toAbsolutePath(), e);
        }
        return uuid;
    }

    private void write(String uuid, Path directory, Path uuidFile) {
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            Files.createFile(UUID_FILE);
            Files.newBufferedWriter(UUID_FILE)
                .append(uuid)
                .close();
        } catch (IOException e) {
            LOGGER.warn("Could not write redhat anonymous UUID to file at " + UUID_FILE.toAbsolutePath(), e);
        }
    }
}
