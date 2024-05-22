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

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

public class MessageLimits {

    private static final Duration DEFAULT_REFRESH_PERIOD = Duration.ofHours(6);
    private static MessageLimits INSTANCE = null;
    private final Configurations configuration;

    static MessageLimits getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MessageLimits(PluginLimitsDeserialization::create, new Configurations());
        }
        return INSTANCE;
    }

    interface PluginLimitsFactory {
        List<PluginLimits> create(String json) throws IOException;
    }

    private final PluginLimitsFactory factory;
    private List<PluginLimits> limits;

    MessageLimits(PluginLimitsFactory factory, Configurations configuration) {
        this.factory = factory;
        this.configuration = configuration;
    }

    List<PluginLimits> get() {
        FileTime lastModified = configuration.getLocalLastModified();
        PluginLimits defaultLimits = getDefaultLimits(limits);
        Duration refreshAfter = getRefreshAfter(defaultLimits);
        if (needsRefresh(refreshAfter, lastModified)) {
            this.limits = createLimits(configuration.downloadRemote(), factory);
        }
        return limits;
    }

    boolean needsRefresh(Duration refreshAfter, FileTime modified) {
        if (modified == null) {
            return true;
        }
        LocalDateTime modificationLocalTime = LocalDateTime.ofInstant(modified.toInstant(), ZoneId.systemDefault());
        LocalDateTime refreshAt = modificationLocalTime.plus(refreshAfter);
        return refreshAt.isBefore(LocalDateTime.now());
    }

    @Nullable
    PluginLimits getDefaultLimits(List<PluginLimits> limits) {
        if (limits == null) {
            return null;
        }
        return limits.stream()
                .filter(PluginLimits::isDefault)
                .findAny()
                .orElse(null);
    }

    Duration getRefreshAfter(PluginLimits limits) {
        if (limits == null) {
            return DEFAULT_REFRESH_PERIOD;
        }
        return Duration.ofHours(limits.getRefresh());
    }

    private List<PluginLimits> createLimits(String config, PluginLimitsFactory factory) {
        if (StringUtil.isEmptyOrSpaces(config)) {
            return Collections.emptyList();
        }
        try {
            return factory.create(config);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

}
