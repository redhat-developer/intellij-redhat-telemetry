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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LimitsConfigurationsIntegrationTest {

    private Path backup = null;

    @BeforeEach
    void beforeEach() throws IOException {
        this.backup = backup(LimitsConfigurations.LOCAL);
    }

    @AfterEach
    void afterEach() {
        restore(backup, LimitsConfigurations.LOCAL);
        System.clearProperty(LimitsConfigurations.SYSTEM_PROP_REMOTE);
    }

    @Test
    public void downloadRemote_writes_remote_to_local_file() {
        // given
        assertThat(Files.exists(LimitsConfigurations.LOCAL)).isFalse();
        LimitsConfigurations configurations = new LimitsConfigurations();
        // when
        configurations.downloadRemote();
        // then
        assertThat(Files.exists(LimitsConfigurations.LOCAL)).isTrue();
    }

    @Test
    public void downloadRemote_returns_content_that_is_equal_to_local_file() throws IOException {
        // given
        LimitsConfigurations configurations = new LimitsConfigurations();
        // when
        String remote = configurations.downloadRemote();
        // then
        String file = toString(LimitsConfigurations.LOCAL);
        assertThat(remote).isEqualTo(file);
    }

    @Test
    public void downloadRemote_returns_content_of_file_provided_in_system_property() throws IOException {
        // given
        LimitsConfigurations configurations = new LimitsConfigurations();
        Path path = createFileContaining(Files.createTempFile(null, null), "bogus");
        String url = path.toUri().toString();
        System.setProperty(LimitsConfigurations.SYSTEM_PROP_REMOTE, url);
        // when
        String remote = configurations.downloadRemote();
        // then
        assertThat(remote).isEqualTo("bogus");
    }

    @Test
    public void downloadRemote_returns_content_of_remote_if_system_property_has_invalid_url() {
        // given
        LimitsConfigurations configurations = new LimitsConfigurations();
        System.setProperty(LimitsConfigurations.SYSTEM_PROP_REMOTE, "noURL");
        String expected = configurations.download(LimitsConfigurations.REMOTE);
        // when
        String remote = configurations.downloadRemote();
        // then
        assertThat(remote).isEqualTo(expected);
    }

    @Test
    public void downloadRemote_returns_content_of_remote_if_system_property_points_to_missing_file() throws MalformedURLException, URISyntaxException {
        // given
        LimitsConfigurations configurations = new LimitsConfigurations();
        URI missingFile = Paths.get(System.getProperty("java.io.tmpdir"), "bogus").toFile().toURI();
        System.setProperty(LimitsConfigurations.SYSTEM_PROP_REMOTE, missingFile.toString());
        String expected = configurations.download(LimitsConfigurations.REMOTE);
        // when
        String remote = configurations.downloadRemote();
        // then
        assertThat(remote).isEqualTo(expected);
    }

    @Test
    public void downloadRemote_returns_content_of_url_pointed_to_by_system_property() throws MalformedURLException, URISyntaxException {
        // given
        LimitsConfigurations configurations = new LimitsConfigurations();
        System.setProperty(LimitsConfigurations.SYSTEM_PROP_REMOTE, "https://www.redhat.com/");
        String expected = configurations.download("https://www.redhat.com/");
        // when
        String remote = configurations.downloadRemote();
        // then
        assertThat(remote).isEqualTo(expected);
    }

    @Test
    public void readLocal_returns_content_of_local_file() throws IOException {
        // given
        String expected = "yoda";
        createFileContaining(LimitsConfigurations.LOCAL, expected);
        LimitsConfigurations configurations = new LimitsConfigurations();
        // when
        String local = configurations.readLocal();
        // then
        assertThat(local).isEqualTo(expected);
    }

    private static Path createFileContaining(Path path, String content) throws IOException {
        return Files.write(path, content.getBytes(), StandardOpenOption.CREATE);
    }

    @Test
    public void readLocalLastModified_returns_time_when_file_was_created() throws IOException {
        // given
        Files.write(LimitsConfigurations.LOCAL, "obiwan".getBytes(), StandardOpenOption.CREATE);
        FileTime whenCreated = Files.getLastModifiedTime(LimitsConfigurations.LOCAL);
        LimitsConfigurations configurations = new LimitsConfigurations();
        // when
        FileTime whenChecked = configurations.getLocalLastModified();
        // then
        assertThat(whenChecked).isEqualTo(whenCreated);
    }

    @Test
    public void readLocalLastModified_returns_time_when_file_was_downloaded() throws IOException {
        // given
        Files.write(LimitsConfigurations.LOCAL, "obiwan".getBytes(), StandardOpenOption.CREATE);
        FileTime whenCreated = Files.getLastModifiedTime(LimitsConfigurations.LOCAL);
        LimitsConfigurations configurations = new LimitsConfigurations();
        configurations.downloadRemote();
        FileTime whenDownloaded = Files.getLastModifiedTime(LimitsConfigurations.LOCAL);
        // when
        FileTime whenChecked = configurations.getLocalLastModified();
        // then
        assertThat(whenCreated.compareTo(whenChecked) < 0).isTrue();
        assertThat(whenChecked).isEqualTo(whenDownloaded);
    }

    @Test
    public void readLocalLastModified_returns_null_if_local_file_does_not_exist() {
        // given
        LimitsConfigurations configurations = new LimitsConfigurations();
        // when
        FileTime whenChecked = configurations.getLocalLastModified();
        // then
        assertThat(whenChecked).isNull();
    }

    @Test
    public void readEmbedded_returns_content_of_embedded_file() throws IOException {
        // given
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                LimitsConfigurationsIntegrationTest.class.getResourceAsStream(LimitsConfigurations.EMBEDDED))));
        String expected = reader.lines().collect(Collectors.joining());
        LimitsConfigurations configurations = new LimitsConfigurations();
        // when
        String local = configurations.readEmbedded();
        // then
        assertThat(local).isEqualTo(expected);
    }

    private String toString(Path path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path)));
        return reader.lines().collect(Collectors.joining());
    }

    private Path backup(Path toBackup) throws IOException {
        Path backup = Files.createTempFile(toBackup.getFileName().toString(), null);
        boolean moved = safeMove(toBackup, backup);
        assertThat(Files.exists(toBackup)).isFalse();
        if (moved) {
            return backup;
        } else {
            return null;
        }
    }

    private void restore(Path backup, Path destination) {
        if (backup == null) {
            return;
        }
        safeMove(backup, destination);
    }

    private boolean safeMove(Path source, Path destination) {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
