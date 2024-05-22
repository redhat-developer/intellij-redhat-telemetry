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
import com.redhat.devtools.intellij.telemetry.core.service.UserId;

import java.util.List;

public class PluginLimits {
    private final String pluginId;
    private final Enabled enabled;
    private final int refresh;
    private final float ratio;
    private final List<Filter> includes;
    private final List<Filter> excludes;
    private final UserId userId;

    PluginLimits(String pluginId, Enabled enabled, int refresh, float ratio, List<Filter> includes, List<Filter> excludes) {
        this(pluginId, enabled, refresh, ratio, includes, excludes, UserId.INSTANCE);
    }

    PluginLimits(String pluginId, Enabled enabled, int refresh, float ratio, List<Filter> includes, List<Filter> excludes, UserId userId) {
        this.pluginId = pluginId;
        this.enabled = enabled;
        this.refresh = refresh;
        this.ratio = ratio;
        this.includes = includes;
        this.excludes = excludes;
        this.userId = userId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public boolean isDefault() {
        return "*".equals(pluginId);
    }

    Enabled getEnabled() {
        return enabled;
    }

    int getRefresh() {
        return refresh;
    }

    float getRatio() {
        return ratio;
    }

    public boolean canSend(Event event, int currentTotal) {
        if (event == null) {
            return false;
        }
        if (!isEnabled()
                || (isErrorOnly() && !event.hasError())) {
            return false;
        }

        if (!isInRatio()) {
            return false;
        }

        return isIncluded(event, currentTotal)
                && !isExcluded(event);
    }

    private boolean isInRatio() {
        if (userId == null) {
            return true;
        }
        return ratio > 0
            && ratio >= userId.getPercentile();
    }

    boolean isEnabled() {
        Enabled enabled = getEnabled();
        return enabled != null
                && enabled != Enabled.OFF;
    }

    boolean isErrorOnly() {
        Enabled enabled = getEnabled();
        return enabled == Enabled.CRASH
                || enabled == Enabled.ERROR;
    }

    List<Filter> getIncludes() {
        return includes;
    }

    boolean isIncluded(Event event, int currentTotal) {
        Filter matching = includes.stream()
                .filter(filter -> filter.isMatching(event))
                .findAny()
                .orElse(null);
        return matching == null ||
                (matching.isIncludedByRatio(userId.getPercentile())
                        && matching.isWithinDailyLimit(currentTotal));
    }

    boolean isExcluded(Event event) {
        Filter matching = excludes.stream()
                .filter(filter -> filter.isMatching(event))
                .findAny()
                .orElse(null);
        return matching != null
                && matching.isExcludedByRatio(userId.getPercentile());
    }

    List<Filter> getExcludes() {
        return excludes;
    }

}