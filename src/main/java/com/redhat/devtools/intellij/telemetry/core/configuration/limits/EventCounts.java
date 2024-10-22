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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.redhat.devtools.intellij.telemetry.core.service.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.redhat.devtools.intellij.telemetry.core.util.TimeUtils.isToday;

/**
 * A counter that stores daily occurrences of events.
 * The state is persisted and loaded by the IDEA platform
 *
 * @see PersistentStateComponent
 */
@Service
@State(
        name = " com.redhat.devtools.intellij.telemetry.core.configuration.limits.EventCounts",
        storages = @Storage(value = "eventCounts.xml")
)
public final class EventCounts implements PersistentStateComponent<EventCounts> {

    public static EventCounts getInstance() {
        return ApplicationManager.getApplication().getService(EventCounts.class);
    }

    private static final String COUNT_VALUES_SEPARATOR = ",";

    EventCounts() {}

    public final Map<String, String> counts = new HashMap<>();

    @Override
    public EventCounts getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull EventCounts state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void noStateLoaded() {
        PersistentStateComponent.super.noStateLoaded();
    }

    @Override
    public void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }

    @Nullable
    public Count get(Event event) {
        if (event == null) {
            return null;
        }
        String countString = counts.get(event.getName());
        return toCount(countString);
    }

    public void put(Event event) {
        Count count = createOrUpdateCount(event);
        put(event, count);
    }

    EventCounts put(Event event, Count count) {
        if (event == null) {
            return this;
        }
        counts.put(event.getName(), toString(count));
        return this;
    }

    private Count createOrUpdateCount(Event event) {
        Count count = get(event);
        if (count != null) {
            // update existing
            count = count.newOccurrence();
        } else {
            // create new
            count = new Count();
        }
        return count;
    }

    private Count toCount(String string) {
        if (StringUtil.isEmpty(string)) {
            return null;
        }
        String[] split = string.split(COUNT_VALUES_SEPARATOR);
        LocalDateTime lastOccurrence = toLastOccurrence(split[0]);
        int total = toTotal(split[1]);
        return new Count(lastOccurrence, total);
    }

    private LocalDateTime toLastOccurrence(String value) {
        try {
            long epochSeconds = Long.parseLong(value);
            return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZonedDateTime.now().getOffset());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int toTotal(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    String toString(@NotNull Count count) {
        long epochSecond = count.lastOccurrence.toEpochSecond(ZonedDateTime.now().getOffset());
        return epochSecond + COUNT_VALUES_SEPARATOR + count.dailyTotal;
    }

    public static class Count {
        private final LocalDateTime lastOccurrence;
        private final int dailyTotal;

        Count() {
            this(LocalDateTime.now(), 1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Count)) return false;
            Count count = (Count) o;
            return dailyTotal == count.dailyTotal && Objects.equals(lastOccurrence, count.lastOccurrence);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lastOccurrence, dailyTotal);
        }

        Count(LocalDateTime lastOccurrence, int dailyTotal) {
            this.lastOccurrence = lastOccurrence;
            this.dailyTotal = dailyTotal;
        }

        public LocalDateTime getLastOccurrence() {
            return lastOccurrence;
        }

        public int getDailyTotal() {
            if (isToday(lastOccurrence)) {
                return dailyTotal;
            } else {
                return 0;
            }
        }

        public Count newOccurrence() {
            return new Count(LocalDateTime.now(), getDailyTotal() + 1);
        }
    }
}
