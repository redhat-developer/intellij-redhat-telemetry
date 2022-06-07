package com.redhat.devtools.intellij.telemetry.core.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

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

    public static void write(String content, Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.append(content);
        }
    }
}
