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
import com.intellij.openapi.util.text.StringUtil;
import com.redhat.devtools.intellij.telemetry.core.service.Event;
import com.redhat.devtools.intellij.telemetry.core.util.TimeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.EventCounts.Count;

public class EventLimits implements IEventLimits {

    private static final Logger LOGGER = Logger.getInstance(EventLimits.class);

    static final Duration DEFAULT_REFRESH_PERIOD = Duration.ofHours(6);
    private final String pluginId;
    private final PluginLimitsFactory factory;
    private final LimitsConfigurations configuration;
    private final EventCounts counts;
    private List<PluginLimits> limits;

    interface PluginLimitsFactory {
        List<PluginLimits> create(String json) throws IOException;
    }

    public EventLimits(String pluginId) {
        this(pluginId, null, PluginLimitsDeserialization::create, new LimitsConfigurations(), EventCounts.getInstance());
    }

    EventLimits(String pluginId,
                List<PluginLimits> limits,
                PluginLimitsFactory factory,
                LimitsConfigurations configuration,
                EventCounts counts) {
        this.pluginId = pluginId;
        this.limits = limits;
        this.factory = factory;
        this.configuration = configuration;
        this.counts = counts;
    }

    public boolean canSend(Event event) {
        List<PluginLimits> all = getAllLimits();
        PluginLimits pluginLimits = getPluginLimits(pluginId, all);
        int total = getApplicableTotal(counts.get(event));
        if (pluginLimits != null) {
            return pluginLimits.canSend(event, total);
        } else {
            PluginLimits defaultLimits = getDefaultLimits(all);
            if (defaultLimits == null) {
                return true;
            }
            return defaultLimits.canSend(event, total);
        }
    }

    public void wasSent(Event event) {
        counts.put(event);
    }

    private int getApplicableTotal(Count count) {
        if (occurredToday(count)) {
            return count.getDailyTotal();
        } else {
            return 0;
        }
    }

    private static boolean occurredToday(Count count) {
        return count != null
                && count.getLastOccurrence() != null
                && TimeUtils.isToday(count.getLastOccurrence());
    }


    /* for testing purposes */
    List<PluginLimits> getAllLimits() {
        PluginLimits defaults = getDefaultLimits(limits);
        Duration refreshAfter = getRefreshAfter(defaults);
        FileTime lastModified = configuration.getLocalLastModified();
        if (needsRefresh(refreshAfter, lastModified)) {
            this.limits = downloadRemote(configuration, factory);
        } else if (limits == null) {
            this.limits = readLocal(configuration, factory);
        }
        return limits;
    }

    private boolean needsRefresh(Duration refreshAfter, FileTime modified) {
        if (modified == null) {
            return true;
        }
        LocalDateTime modificationLocalTime = LocalDateTime.ofInstant(modified.toInstant(), ZoneId.systemDefault());
        LocalDateTime refreshAt = modificationLocalTime.plus(refreshAfter);
        return refreshAt.isBefore(LocalDateTime.now());
    }

    @Nullable
    private PluginLimits getDefaultLimits(List<PluginLimits> limits) {
        if (limits == null) {
            return null;
        }
        return limits.stream()
                .filter(PluginLimits::isDefault)
                .findAny()
                .orElse(null);
    }

    @Nullable
    private PluginLimits getPluginLimits(String pluginId, List<PluginLimits> allLimits) {
        if (allLimits == null
                || StringUtil.isEmptyOrSpaces(pluginId)) {
            return null;
        }
        return allLimits.stream()
                .filter(limits -> pluginId.equals(limits.getPluginId()))
                .findAny()
                .orElse(null);
    }

    @NotNull
    private Duration getRefreshAfter(PluginLimits defaults) {
        if (defaults == null
                || defaults.getRefresh() == -1) {
            return DEFAULT_REFRESH_PERIOD;
        }
        return Duration.ofHours(defaults.getRefresh());
    }

    private List<PluginLimits> readLocal(LimitsConfigurations configuration, PluginLimitsFactory factory) {
        try {
            String config = configuration.readLocal();
            if (StringUtil.isEmptyOrSpaces(config)) {
                return downloadRemote(configuration, factory);
            }
            return factory.create(config);
        } catch (Exception e) {
            return downloadRemote(configuration, factory);
        }
    }

    @Nullable
    private List<PluginLimits> downloadRemote(LimitsConfigurations configuration, PluginLimitsFactory factory) {
        try {
            String config = configuration.downloadRemote();
            if (StringUtil.isEmptyOrSpaces(config)) {
                return createEmbeddedLimits(configuration, factory);
            }
            return factory.create(config);
        } catch (Exception e) {
            return createEmbeddedLimits(configuration, factory);
        }
    }

    private List<PluginLimits> createEmbeddedLimits(LimitsConfigurations configuration, PluginLimitsFactory factory) {
        try {
            return factory.create(configuration.readEmbedded());
        } catch (IOException e) {
            return null;
        }
    }
}
