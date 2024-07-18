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

import com.redhat.devtools.intellij.telemetry.core.service.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.EventLimits.PluginLimitsFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EventLimitsTest {

    private static final String LOCAL = "{\n" +
            "  \"*\": {\n" +
            "    \"enabled\": \"all\",\n" +
            "    \"includes\": [\n" +
            "      {\n" +
            "        \"name\": \"*\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"excludes\": []\n" +
            "  }\n" +
            "}";

    private List<PluginLimits> localLimits;

    private static final String REMOTE = "{\n" +
            "  \"*\": {\n" +
            "    \"enabled\": \"error\",\n" +
            "    \"includes\": [\n" +
            "      {\n" +
            "        \"name\": \"startup\",\n" +
            "        \"dailyLimit\": 1\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"*\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"excludes\": [\n" +
            "      {\n" +
            "        \"name\": \"shutdown\",\n" +
            "        \"ratio\": \"1.0\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    private List<PluginLimits> remoteLimits;

    private static final String EMBEDDED = "{\n" +
            "  \"*\": {\n" +
            "    \"enabled\": \"all\",\n" +
            "    \"refresh\": \"12h\",\n" +
            "    \"includes\": [],\n" +
            "    \"excludes\": []\n" +
            "  }\n" +
            "}";

    private List<PluginLimits> embeddedLimits;

    @BeforeEach
    void beforeEach() throws IOException {
        this.localLimits = PluginLimitsDeserialization.create(LOCAL);
        this.remoteLimits = PluginLimitsDeserialization.create(REMOTE);
        this.embeddedLimits = PluginLimitsDeserialization.create(EMBEDDED);
    }

    @Test
    public void getAllLimits_returns_null_if_deserialization_throws() throws IOException {
        // given
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        doThrow(new IOException())
                .when(factory).create(any());
        LimitsConfigurations configurations = mock(LimitsConfigurations.class);
        doReturn("bogus") // needs to return non-null for factory to be invoked
                .when(configurations).downloadRemote();
        EventCounts counts = mock(EventCounts.class);
        EventLimits limits = new EventLimits("bogus", null, factory, configurations, counts);
        // when
        List<PluginLimits> pluginLimits = limits.getAllLimits();
        // then
        assertThat(pluginLimits).isNull();
    }

    @Test
    public void getAllLimits_downloads_remote_if_local_file_has_no_modification_timestamp() {
        // given
        List<PluginLimits> allLimits = List.of(createDefaultPluginLimits(Integer.MAX_VALUE));
        LimitsConfigurations configurations = mock(LimitsConfigurations.class);
        doReturn(null) // no modification timestamp, file does not exist
                .when(configurations).getLocalLastModified();
        EventCounts counts = mock(EventCounts.class);
        EventLimits limits = new EventLimits("bogus", allLimits, mock(PluginLimitsFactory.class), configurations, counts);
        // when
        limits.getAllLimits();
        // then
        verify(configurations).downloadRemote();
    }

    @Test
    public void getAllLimits_downloads_remote_if_local_file_was_modified_7h_ago_and_no_default_limits_exist() {
        // given
        List<PluginLimits> noDefaultLimits = Collections.emptyList();
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        LimitsConfigurations configurations = mock(LimitsConfigurations.class);
        EventCounts counts = mock(EventCounts.class);
        // default refresh (without existing plugin limits) is 6h
        doReturn(createFileTime(7)) // 7h ago
                .when(configurations).getLocalLastModified();
        EventLimits limits = new EventLimits("bogus", noDefaultLimits, factory, configurations, counts);
        // when
        limits.getAllLimits();
        // then
        verify(configurations).downloadRemote();
    }

    @Test
    public void getAllLimits_downloads_remote_if_local_file_was_modified_7h_ago_and_default_limits_has_no_refresh() {
        // given
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        LimitsConfigurations configurations = mock(LimitsConfigurations.class);
        doReturn(createFileTime(7)) // 7h ago. Refresh needed, default refresh is 6h
                .when(configurations).getLocalLastModified();
        EventLimits limits = new EventLimits("bogus",
                null, // no configuration read yet
                factory,
                configurations,
                mock(EventCounts.class));
        // when
        limits.getAllLimits();
        // then
        verify(configurations).downloadRemote();
    }

    @Test
    public void getAllLimits_reads_local_config_and_does_NOT_download_remote_if_local_config_was_modified_within_specified_refresh_period() throws IOException {
        // given
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        LimitsConfigurations configurations = mock(LimitsConfigurations.class);
        doReturn(createFileTime(1)) // 1h ago
                .when(configurations).getLocalLastModified();
        doReturn(LOCAL) // 1h ago
                .when(configurations).readLocal();
        doReturn(localLimits)
                .when(factory).create(LOCAL);

        EventLimits limits = new EventLimits("bogus",
                null,  // no configuration read yet
                factory,
                configurations,
                mock(EventCounts.class));
        // when
        List<PluginLimits> pluginLimits = limits.getAllLimits();
        // then
        verify(configurations).readLocal();
        verify(configurations, never()).downloadRemote();
        verify(configurations, never()).readEmbedded();
        assertThat(pluginLimits).isEqualTo(localLimits);
    }

    @Test
    public void getAllLimits_returns_embedded_config_if_downloadRemote_returns_null() throws IOException {
        // given
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        LimitsConfigurations configurations = mock(LimitsConfigurations.class);
        doReturn(null) // no local file present
                .when(configurations).getLocalLastModified();
        doReturn(null) // no remote config
                .when(configurations).downloadRemote();
        doReturn(EMBEDDED)
                .when(configurations).readEmbedded();
        doReturn(embeddedLimits)
                .when(factory).create(EMBEDDED);
        EventLimits limits = new EventLimits("bogus",
                null, // no configuration read yet
                factory,
                configurations,
                mock(EventCounts.class));
        // when
        List<PluginLimits> pluginLimits = limits.getAllLimits();
        // then
        assertThat(pluginLimits).isEqualTo(embeddedLimits);
    }

    @Test
    public void getAllLimits_downloads_remote_config_if_local_cannot_be_parsed() throws IOException {
        // given
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        LimitsConfigurations configurations = createConfigurations(LocalDateTime.now()); // no refresh needed
        doReturn(LOCAL)
                .when(configurations).readLocal();
        doThrow(IOException.class) // parsing local file fails
                .when(factory).create(LOCAL);
        doReturn(REMOTE)
                .when(configurations).downloadRemote();
        doReturn(remoteLimits)
                .when(factory).create(REMOTE);
        doReturn(EMBEDDED)
                .when(configurations).readEmbedded();
        doReturn(embeddedLimits)
                .when(factory).create(EMBEDDED);
        EventLimits limits = new EventLimits("bogus",
                null, // no configuration read yet
                factory,
                configurations,
                mock(EventCounts.class));
        // when
        List<PluginLimits> pluginLimits = limits.getAllLimits();
        // then
        assertThat(pluginLimits).isEqualTo(remoteLimits);
    }

    @Test
    public void getAllLimits_reads_embedded_if_local_cannot_be_parsed_and_remote_is_not_downloadable() throws IOException {
        // given
        PluginLimitsFactory factory = mock(PluginLimitsFactory.class);
        LimitsConfigurations configurations = createConfigurations(LocalDateTime.now()); // no refresh needed
        doReturn(LOCAL)
                .when(configurations).readLocal();
        doThrow(IOException.class) // parsing local file fails, parsing remote/embedded doesn't throw
                .when(factory).create(LOCAL);
        doReturn(null)
                .when(configurations).downloadRemote();
        doReturn(EMBEDDED)
                .when(configurations).readEmbedded();
        doReturn(embeddedLimits)
                .when(factory).create(EMBEDDED);
        EventLimits limits = new EventLimits("bogus",
                null, // no configuration read yet
                factory,
                configurations,
                mock(EventCounts.class));
        // when
        List<PluginLimits> pluginLimits = limits.getAllLimits();
        // then
        assertThat(pluginLimits).isEqualTo(embeddedLimits);
    }

    @Test
    public void canSend_returns_true_if_default_allows() throws IOException {
        // given
        LimitsConfigurations configurations = createConfigurations(LocalDateTime.now()); // local file up-to-date, no refresh
        List<PluginLimits> pluginLimits = List.of(createDefaultPluginLimits(true));
        EventLimits limits = new EventLimits(
                "bogus",
                pluginLimits,
                null,
                configurations,
                mock(EventCounts.class));
        Event event = new Event(Event.Type.USER, "luke");
        // when
        boolean canSend = limits.canSend(event);
        // then
        assertThat(canSend).isTrue();
    }

    @Test
    public void canSend_returns_true_if_default_cannotSend_but_pluginLimit_canSend() throws IOException {
        // given
        String pluginId = "jedis";
        LimitsConfigurations configurations = createConfigurations(LocalDateTime.now()); // local file up-to-date, no refresh
        List<PluginLimits> pluginLimits = List.of(
                createDefaultPluginLimits(Integer.MAX_VALUE, false),
                createPluginLimits(pluginId, Integer.MAX_VALUE, true)
        );
        EventLimits limits = new EventLimits(
                pluginId,
                pluginLimits,
                null,
                configurations,
                mock(EventCounts.class));
        Event event = new Event(Event.Type.USER, "luke");
        // when
        boolean canSend = limits.canSend(event);
        // then
        assertThat(canSend).isTrue();
    }

    @Test
    public void canSend_returns_true_if_there_is_no_default_nor_pluginLimit() throws IOException {
        // given
        String pluginId = "jedis";
        LimitsConfigurations configurations = createConfigurations(LocalDateTime.now()); // local file up-to-date, no refresh
        List<PluginLimits> pluginLimits = Collections.emptyList();
        EventLimits limits = new EventLimits(
                pluginId,
                pluginLimits,
                null,
                configurations,
                mock(EventCounts.class));
        Event event = new Event(Event.Type.USER, "luke");
        // when
        boolean canSend = limits.canSend(event);
        // then
        assertThat(canSend).isTrue();
    }

    @Test
    public void wasSent_puts_event_to_eventCount() throws IOException {
        // given
        String pluginId = "jedis";
        LimitsConfigurations configurations = createConfigurations(LocalDateTime.now()); // local file up-to-date, no refresh
        List<PluginLimits> pluginLimits = Collections.emptyList();
        EventCounts eventCounts = mock(EventCounts.class);
        EventLimits limits = new EventLimits(
                pluginId,
                pluginLimits,
                null,
                configurations,
                eventCounts);
        Event event = new Event(Event.Type.USER, "yoda");
        // when
        limits.wasSent(event);
        // then
        verify(eventCounts).put(event);
    }

    private static FileTime createFileTime(int createdHoursAgo) {
        return FileTime.from(
                Instant.now().minus(createdHoursAgo, ChronoUnit.HOURS));
    }

    private static PluginLimits createDefaultPluginLimits(boolean canSend) {
        return createDefaultPluginLimits(-1, canSend); // no refresh
    }

    private static PluginLimits createDefaultPluginLimits(int refresh) {
        return createDefaultPluginLimits(refresh, false);
    }

    private static PluginLimits createDefaultPluginLimits(int refresh, boolean canSend) {
        PluginLimits mock = createPluginLimits("*", refresh, canSend);
        doReturn(true)
                .when(mock).isDefault();
        return mock;
    }

    private static PluginLimits createPluginLimits(String pluginId, int refresh, boolean canSend) {
        PluginLimits mock = mock(PluginLimits.class);
        doReturn(pluginId)
                .when(mock).getPluginId();
        doReturn(refresh)
                .when(mock).getRefresh();
        doReturn(canSend)
                .when(mock).canSend(any(), anyInt());
        return mock;
    }

    private static LimitsConfigurations createConfigurations(@NotNull LocalDateTime localModificationTimestamp) {
        LimitsConfigurations configurations = mock(LimitsConfigurations.class);
        int localCreatedHoursAgo = (int) ChronoUnit.HOURS.between(localModificationTimestamp, LocalDateTime.now());
        doReturn(createFileTime(localCreatedHoursAgo))
                .when(configurations).getLocalLastModified();
        return configurations;
    }

}
