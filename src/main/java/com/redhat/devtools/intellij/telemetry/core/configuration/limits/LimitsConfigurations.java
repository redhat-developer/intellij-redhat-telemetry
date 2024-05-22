/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.configuration.limits;

import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.util.Directories;
import com.redhat.devtools.intellij.telemetry.core.util.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.telemetry.core.util.FileUtils.ensureExists;
import static com.redhat.devtools.intellij.telemetry.core.util.FileUtils.getPathForFileUrl;

class LimitsConfigurations {

    private static final Logger LOGGER = Logger.getInstance(LimitsConfigurations.class);
    static final Path LOCAL = Directories.RED_HAT.resolve("telemetry-config.json");
    static final String EMBEDDED = "/telemetry-config.json";
    static final String REMOTE = "https://raw.githubusercontent.com/adietish/intellij-redhat-telemetry/issue-82/src/main/resources/telemetry-config.json";
    static final String SYSTEM_PROP_REMOTE = "REDHAT_TELEMETRY_REMOTE_CONFIG_URL";

    protected final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

    @Nullable
    public String downloadRemote() {
        String url = System.getProperty(SYSTEM_PROP_REMOTE);

        String content = readFile(url);
        if (content != null) {
            return content;
        }

        if (FileUtils.isFileUrl(url)
                || !isValidURL(url)) {
            // file-url to missing/unreadable file or missing/invalid url
            url = REMOTE;
        }

        return download(url);
    }

    private @Nullable String readFile(String url) {
        Path path = getPathForFileUrl(url);
        if (path == null) {
            return null;
        }
        try {
            return toString(Files.newInputStream(path));
        } catch (IOException e) {
            LOGGER.warn("Could not read remote limits configurations file from " + path, e);
            return null;
        }
    }

    @Nullable String download(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                Files.copy(response.body().byteStream(), ensureExists(LOCAL), StandardCopyOption.REPLACE_EXISTING);
            }
            return readLocal();
        } catch (Exception e) {
            LOGGER.warn("Could not download remote limits configurations from " + url, e);
            return null;
        }
    }

    @Nullable
    public FileTime getLocalLastModified() {
        try {
            if (!Files.exists(LOCAL)) {
                return null;
            }
            return Files.getLastModifiedTime(LOCAL);
        } catch (Throwable e) {
            return null;
        }
    }

    @Nullable
    public String readLocal() {
        try {
            return toString(Files.newInputStream(LOCAL));
        } catch (IOException e) {
            return null;
        }
    }

    public String readEmbedded() throws IOException {
        return toString(LimitsConfigurations.class.getResourceAsStream(EMBEDDED));
    }

    private String toString(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

}