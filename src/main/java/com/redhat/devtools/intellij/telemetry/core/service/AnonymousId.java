package com.redhat.devtools.intellij.telemetry.core.service;

import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class AnonymousId {

    private static final Logger LOGGER = Logger.getInstance(AnonymousId.class);

    public static final AnonymousId INSTANCE = new AnonymousId();
    private static final Path REDHAT_DIRECTORY = Paths.get(System.getProperty("user.home"), ".redhat");
    private static final Path UUID_FILE = REDHAT_DIRECTORY.resolve("anonymousId");

    private final Lazy<String> uuid = new Lazy<>(() -> loadOrCreate(UUID_FILE));

    private AnonymousId() {}

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

    private void write(String uuid, Path uuidFile) {
        try {
            if (!Files.exists(uuidFile.getParent())) {
                Files.createDirectories(uuidFile.getParent());
            }
            Files.createFile(uuidFile);
            try (Writer writer = Files.newBufferedWriter(uuidFile)) {
                writer.append(uuid);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not write redhat anonymous UUID to file at " + UUID_FILE.toAbsolutePath(), e);
        }
    }
}
