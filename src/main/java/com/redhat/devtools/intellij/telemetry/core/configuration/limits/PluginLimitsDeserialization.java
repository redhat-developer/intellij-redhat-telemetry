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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.intellij.openapi.util.text.StringUtil;
import com.redhat.devtools.intellij.telemetry.core.configuration.limits.Filter.EventNameFilter;
import com.redhat.devtools.intellij.telemetry.core.configuration.limits.Filter.EventPropertyFilter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class PluginLimitsDeserialization extends StdDeserializer<List<PluginLimits>> {

    public static final String FIELDNAME_ENABLED = "enabled";
    public static final String FIELDNAME_REFRESH = "refresh";
    public static final String FIELDNAME_RATIO = "ratio";
    public static final String FIELDNAME_INCLUDES = "includes";
    public static final String FIELDNAME_EXCLUDES = "excludes";
    public static final String FIELDNAME_PROPERTY = "property";
    public static final String FIELDNAME_VALUE = "value";
    public static final String FIELDNAME_DAILY_LIMIT = "dailyLimit";
    public static final String FIELDNAME_NAME = "name";

    public static List<PluginLimits> create(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, new PluginLimitsDeserialization());
        mapper.registerModule(module);
        return mapper.readValue(json, List.class);
    }

    PluginLimitsDeserialization() {
        this(null);
    }

    PluginLimitsDeserialization(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public List<PluginLimits> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        Spliterator<Map.Entry<String, JsonNode>> spliterator = Spliterators.spliteratorUnknownSize(node.fields(), Spliterator.IMMUTABLE);
        return StreamSupport.stream(spliterator, false)
                .map(this::createMessageLimit)
                .collect(Collectors.toList());
    }

    @NotNull
    private PluginLimits createMessageLimit(Map.Entry<String, JsonNode> entry) {
        String pattern = entry.getKey();
        JsonNode properties = entry.getValue();
        Enabled enabled = getEnabled(properties.get(FIELDNAME_ENABLED));
        int refresh = getRefresh(properties.get(FIELDNAME_REFRESH));
        float ratio = getRatio(properties.get(FIELDNAME_RATIO));
        List<Filter> includes = getFilters(properties.get(FIELDNAME_INCLUDES));
        List<Filter> excludes = getFilters(properties.get(FIELDNAME_EXCLUDES));

        return new PluginLimits(pattern, enabled, refresh, ratio, includes, excludes);
    }

    private Enabled getEnabled(JsonNode node) {
        String value = node != null ? node.asText() : null;
        return Enabled.safeValueOf(value);
    }

    private int getRefresh(JsonNode node) {
        int numeric = -1;
        if (node != null) {
            String refresh = getNumericPortion(node.asText().toCharArray());
            if (!StringUtil.isEmptyOrSpaces(refresh)) {
                try {
                    numeric = Integer.parseInt(refresh);
                } catch (NumberFormatException e) {
                    // swallow
                }
            }
        }
        return numeric;
    }

    private List<Filter> getFilters(JsonNode node) {
        if (node == null
                || !node.isArray()) {
            return Collections.emptyList();
        }
        Spliterator<JsonNode> spliterator = Spliterators.spliteratorUnknownSize(node.elements(), Spliterator.IMMUTABLE);
        return StreamSupport.stream(spliterator, false)
                .map(this::createMessageLimitFilter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Filter createMessageLimitFilter(JsonNode node) {
        if (node.has(FIELDNAME_NAME)) {
            return createEventNameFilter(node);
        } else if (node.has(FIELDNAME_PROPERTY)
                && node.has(FIELDNAME_VALUE)) {
            return createEventPropertyFilter(node);
        } else {
            return null;
        }
    }

    private EventNameFilter createEventNameFilter(JsonNode node) {
        String name = getStringValue(FIELDNAME_NAME, node);
        float ratio = getRatio(node.get(FIELDNAME_RATIO));
        String dailyLimit = getStringValue(FIELDNAME_DAILY_LIMIT, node);
        return new EventNameFilter(name, ratio, dailyLimit);
    }

    private EventPropertyFilter createEventPropertyFilter(JsonNode node) {
        String property = getStringValue(FIELDNAME_PROPERTY, node);
        String value = getStringValue(FIELDNAME_VALUE, node);
        return new EventPropertyFilter(property, value);
    }

    private static float getRatio(JsonNode node) {
        float numeric = 1f;
        if (node != null) {
            try {
                numeric = Float.parseFloat(node.asText());
            } catch (NumberFormatException e) {
                // swallow
            }
        }
        return numeric;
    }

    private static String getStringValue(String name, JsonNode node) {
        if (node == null
                || node.get(name) == null) {
            return null;
        }
        return node.get(name).asText();
    }

    private static String getNumericPortion(char[] characters) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < characters.length && Character.isDigit(characters[i]); i++) {
            builder.append(characters[i]);
        }
        return builder.toString();
    }
}