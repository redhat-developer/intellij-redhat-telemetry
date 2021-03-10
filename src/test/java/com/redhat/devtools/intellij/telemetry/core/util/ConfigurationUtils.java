package com.redhat.devtools.intellij.telemetry.core.util;

import com.intellij.openapi.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ConfigurationUtils {

    public static void createPropertyFile(Path path, Pair... keyValues) throws IOException {
        File file = path.toFile();
        file.createNewFile();
        file.deleteOnExit();
        String content = Arrays.stream(keyValues)
                .map(keyValue -> keyValue.first + "=" + keyValue.second)
                .collect(Collectors.joining("\n"));
        try (OutputStream stream = Files.newOutputStream(path)) {
            stream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }


}
