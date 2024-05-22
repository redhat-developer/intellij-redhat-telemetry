package com.redhat.devtools.intellij.telemetry.core.configuration.limits;

import com.redhat.devtools.intellij.telemetry.core.util.Directories;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class Configurations {

    private static final Path LOCAL = Directories.RED_HAT.resolve("telemetry-config.json");
    private static final String EMBEDDED = "/telemetry-config.json";
    private static final String REMOTE = "https://raw.githubusercontent.com/redhat-developer/vscode-redhat-telemetry/main/src/config/telemetry-config.json";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

    public String downloadRemote() {
        Request request = new Request.Builder()
                .url(REMOTE)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                Files.copy(response.body().byteStream(), LOCAL, StandardCopyOption.REPLACE_EXISTING);
            }
            return readLocal();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean localExists() {
        return Files.exists(LOCAL);
    }

    public FileTime getLocalLastModified() {
        try {
            BasicFileAttributes attributes = Files.readAttributes(LOCAL, BasicFileAttributes.class);
            return attributes.lastModifiedTime();
        } catch (IOException e) {
            return null;
        }
    }

    public String readLocal() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(LOCAL)));
            return reader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            return null;
        }
    }

    public String readEmbedded() {
        InputStream inputStream = MessageLimits.class.getResourceAsStream(EMBEDDED);
        if (inputStream == null) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines().collect(Collectors.joining());
    }
}