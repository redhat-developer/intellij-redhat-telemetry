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

import com.intellij.configurationStore.JbXmlOutputter;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.xmlb.XmlSerializer;
import com.redhat.devtools.intellij.telemetry.core.service.Event;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static com.redhat.devtools.intellij.telemetry.core.configuration.limits.EventCounts.Count;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Platform Tests for the {@link EventCounts} service.
 * It's extending {@link BasePlatformTestCase} which is using junit 4.
 */
public class EventCountsTest extends BasePlatformTestCase {

    private final Event event1 = new Event(Event.Type.USER, "1");
    private final Count count1 = new Count(LocalDateTime.of(2023,4,4,4,4,4), 42);
    private final Event event2 = new Event(Event.Type.USER, "2");
    private final Count count2 = new Count(LocalDateTime.of(2023, 8, 8, 8, 8, 8), 84);
    private final EventCounts with2Properties = new EventCounts()
            .put(event1, count1)
            .put(event2, count2);

    private final String serializedWith2Properties =
            "<EventCounts>\n" +
            "  <option name=\"counts\">\n" +
            "    <map>\n" +
            "      <entry key=\"1\" value=\"1680573844,42\" />\n" +
            "      <entry key=\"2\" value=\"1691474888,84\" />\n" +
            "    </map>\n" +
            "  </option>\n" +
            "</EventCounts>";


    public void test_getInstance_should_return_instance() {
        // given
        // when
        EventCounts counts = EventCounts.getInstance();
        // then
        assertThat(counts).isNotNull();
    }

    public void test_should_serialize_EventCounts_with_2_properties() throws IOException {
        // given
        // when
        Element element = XmlSerializer.serialize(with2Properties);
        // then
        String xml = toXML(element);
        assertThat(xml).isEqualTo(serializedWith2Properties);
    }

    public void test_should_deserialize_EventCounts_with_2_properties() throws IOException, JDOMException {
        // given
        Document document = toDocument(serializedWith2Properties);
        // when
        EventCounts counts = XmlSerializer.deserialize(document, EventCounts.class);
        // then
        assertThat(counts.counts).isEqualTo(with2Properties.counts);
    }

    public void test_loadState_should_have_2_properties()  {
        // given
        EventCounts counts = new EventCounts();
        assertThat(counts.counts.size()).isEqualTo(0);
        // when
        counts.loadState(with2Properties);
        // then
        assertThat(counts.counts).isEqualTo(with2Properties.counts);
    }

    public void test_get_should_return_count() {
        // given
        // when
        Count found = with2Properties.get(event2);
        // then
        assertThat(found).isEqualTo(count2);
    }

    public void test_get_should_return_null_if_event_is_null() {
        // given
        // when
        Count found = with2Properties.get(null);
        // then
        assertThat(found).isNull();
    }

    public void test_get_should_return_null_if_event_was_not_put_beforehand() {
        // given
        Event bogus = new Event(Event.Type.USER, "bogus");
        // when
        Count found = with2Properties.get(bogus);
        // then
        assertThat(found).isNull();
    }

    public void test_put_should_create_new_count_if_it_doesnt_exist_yet() {
        // given
        EventCounts counts = new EventCounts();
        Count existing = counts.get(event1);
        assertThat(existing).isNull();
        // when
        counts.put(event1);
        // then
        existing = counts.get(event1);
        assertThat(existing).isNotNull();
    }

    public void test_put_should_create_new_count_with_total_of_1() {
        // given
        EventCounts counts = new EventCounts();
        Count existing = counts.get(event1);
        assertThat(existing).isNull();
        // when
        counts.put(event1);
        // then
        existing = counts.get(event1);
        assertThat(existing.getDailyTotal()).isEqualTo(1);
    }

    public void test_put_should_update_existing_count_if_it_already_existed() throws InterruptedException {
        // given
        EventCounts counts = new EventCounts();
        counts.put(event1);
        Count count = counts.get(event1);
        assertThat(count).isNotNull();
        LocalDateTime previousOccurrence = count.getLastOccurrence();
        int previousTotal = count.getDailyTotal();
        Thread.sleep(1000); // wait for 1s, timestamp is in seconds only
        // when
        counts.put(event1);
        // then
        count = counts.get(event1);
        assertThat(count.getLastOccurrence()).isAfter(previousOccurrence);
        assertThat(count.getDailyTotal()).isEqualTo(previousTotal + 1);
    }

    public void test_put_should_reset_count_to_0_existing_count_was_not_today() {
        // given
        EventCounts counts = new EventCounts();
        int previousTotal = 42;
        LocalDateTime previousOccurrence = LocalDateTime.now().minus(Period.ofDays(1));
        Count count = new Count(previousOccurrence, previousTotal);
        counts.put(event1, count);
        // when
        counts.put(event1);
        // then
        count = counts.get(event1);
        assertThat(count.getLastOccurrence()).isAfter(previousOccurrence);
        assertThat(count.getDailyTotal()).isEqualTo(1);
    }

    public void test_put_of_different_event_should_not_affect_existing_count_for_event() {
        // given
        EventCounts counts = new EventCounts();
        counts.put(event1);
        Count count = counts.get(event1);
        assertThat(count).isNotNull();
        LocalDateTime existingLastOccurrence = count.getLastOccurrence();
        int existingTotal = count.getDailyTotal();
        // when
        counts.put(event2);
        // then
        count = counts.get(event1);
        assertThat(count.getLastOccurrence()).isEqualTo(existingLastOccurrence);
        assertThat(count.getDailyTotal()).isEqualTo(existingTotal);
    }

    private Document toDocument(String string) throws JDOMException, IOException {
        InputSource source = new InputSource();
        source.setCharacterStream(new StringReader(string));
        return new SAXBuilder().build(source);
    }

    private static String toXML(Element element) throws IOException {
        StringWriter writer = new StringWriter();
        new JbXmlOutputter().output(element, writer);
        return writer.toString();
    }
}
