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

import com.redhat.devtools.intellij.telemetry.core.configuration.limits.Filter.EventNameFilter;
import com.redhat.devtools.intellij.telemetry.core.service.Event;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.Filter.*;
import static org.assertj.core.api.Assertions.assertThat;

public class EventPropertyFilterTest {

    @Test
    public void isMatching_should_match_any_event_with_exact_property_name_and_value() {
        // given
        Filter filter = new EventPropertyFilter("yoda", "jedi");
        Event event = new Event(Event.Type.USER, "there are jedis in the rebellion", Map.of(
                "yoda", "jedi"));
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isTrue();
    }

    @Test
    public void isMatching_should_match_any_event_with_exact_property_name_and_wildcard_value() {
        // given
        Filter filter = new EventPropertyFilter("yoda", "*jedi*");
        Event event = new Event(Event.Type.USER, "there are jedis on both sides", Map.of(
                "yoda", "is a master jedi!"));
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isTrue();
    }

    @Test
    public void isMatching_should_NOT_match_event_that_doesnt_have_given_property_name() {
        // given
        Filter filter = new EventPropertyFilter("yoda", "*jedi*");
        Event event = new Event(Event.Type.USER, "there are jedis on both sides", Map.of(
                "darth vader", "is a master jedi!")); // key doesnt match
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isFalse();
    }

    @Test
    public void isMatching_should_NOT_match_event_that_has_given_property_name_but_doesnt_have_property_value() {
        // given
        Filter filter = new EventPropertyFilter("yoda", "*jedi*");
        Event event = new Event(Event.Type.USER, "there are jedis on both sides", Map.of(
                "yoda", "is stronger than the emperor")); // value doesnt match
        // when
        boolean matching = filter.isMatching(event);
        // then
        assertThat(matching).isFalse();
    }

}
