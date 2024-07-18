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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginLimitsDeserializationTest {

    @Test
    public void get_should_return_3_limits_if_3_plugins_are_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {}," +
                "  \"yoda\": {},"+
                "  \"obiwan\": {} "+
                "}";
        // when
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        // then
        assertThat(limits).hasSize(3);
    }

    @Test
    public void getEnabled_should_return_ALL_if_no_value_present() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {" +
                "    }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        Enabled enabled = limit.getEnabled();
        // then
        assertThat(enabled).isEqualTo(Enabled.ALL);
    }

    @Test
    public void getEnabled_should_return_ALL_if_unknown_value_present() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {" +
                "            \"enabled\" : \"bogus\"" +
                "  }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        Enabled enabled = limit.getEnabled();
        // then
        assertThat(enabled).isEqualTo(Enabled.ALL);
    }

    @Test
    public void getEnabled_should_return_ERROR_if_error_is_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {" +
                "            \"enabled\" : \"error\"" +
                "    }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        Enabled enabled = limit.getEnabled();
        // then
        assertThat(enabled).isEqualTo(Enabled.ERROR);
    }

    @Test
    public void getRefresh_should_return_negative_refresh_if_no_refresh_is_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {}" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        int refresh = limit.getRefresh();
        // then
        assertThat(refresh).isNegative();
    }

    @Test
    public void getRefresh_should_return_negative_refresh_if_non_numeric_refresh_is_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {\n" +
                "    \"refresh\": \"bogus\"\n" +
                "    }\n" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        int refresh = limit.getRefresh();
        // then
        assertThat(refresh).isNegative();
    }

    @Test
    public void getRefresh_should_return_numeric_portion_of_value_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {\n" +
                "    \"refresh\": \"12h\"\n" +
                "    }\n" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        int refresh = limit.getRefresh();
        // then
        assertThat(refresh).isEqualTo(12);
    }

    @Test
    public void getRefresh_should_return_numeric_value_specified_as_refresh_value() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {\n" +
                "    \"refresh\": \"42\"\n" +
                "    }\n" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        int refresh = limit.getRefresh();
        // then
        assertThat(refresh).isEqualTo(42);
    }

    @Test
    public void getRatio_should_return_float_value_specified_as_ratio_value() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {\n" +
                "    \"ratio\": \"0.42\"\n" +
                "    }\n" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        float ratio = limit.getRatio();
        // then
        assertThat(ratio).isEqualTo(0.42f);
    }

    @Test
    public void getRatio_should_return_1_if_no_ratio_is_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "  \"*\": {\n" +
                "    }\n" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        float ratio = limit.getRatio();
        // then
        assertThat(ratio).isEqualTo(1f);
    }

    @Test
    public void getIncludes_should_return_no_filters_if_no_includes_are_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "    \"*\": {}" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        List<Filter> includes = limit.getIncludes();
        // then
        assertThat(includes).isEmpty();
    }

    @Test
    public void getIncludes_should_return_no_filters_if_bogus_includes_are_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "    \"*\": {\n" +
                "              \"includes\": [\"bogus\"]" +
                "           }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0);
        // when
        List<Filter> includes = limit.getIncludes();
        // then
        assertThat(includes).isEmpty();
    }

    @Test
    public void getIncludes_should_return_3_filters() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "    \"*\": {},\n" +
                "    \"jedis\": {\n" +
                "        \"includes\": [\n" +
                "            {\n" +
                "                \"name\" : \"yoda\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\" : \"obiwan\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\" : \"*\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(1); // jedis
        // when
        List<Filter> includes = limit.getIncludes();
        // then
        assertThat(includes).hasSize(3);
    }

    @Test
    public void getIncludes_should_return_1_event_name_filter() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "    \"*\": {\n" +
                "        \"includes\": [\n" +
                "            {\n" +
                "                \"name\" : \"yoda\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0); // *
        // when
        List<Filter> includes = limit.getIncludes();
        // then
        assertThat(includes).are(
                new Condition<>((filter) -> filter instanceof Filter.EventNameFilter, "is EventNameFilter"));
    }

    @Test
    public void getIncludes_should_return_event_name_filter_with_negative_daily_limit_if_daily_limit_is_not_numeric() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "    \"*\": {\n" +
                "        \"includes\": [\n" +
                "            {\n" +
                "                \"name\" : \"yoda\",\n" +
                "                \"dailyLimit\" : \"bogus\"\n" + // not numeric
                "            }\n" +
                "        ]\n" +
                "    }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0); // *
        List<Filter> includes = limit.getIncludes();
        assertThat(includes).hasSize(1);
        Filter filter = includes.get(0);
        assertThat(filter).isExactlyInstanceOf(Filter.EventNameFilter.class);
        Filter.EventNameFilter nameFilter = (Filter.EventNameFilter) filter;
        // when
        int dailyLimit = nameFilter.getDailyLimit();
        // then
        assertThat(dailyLimit).isEqualTo(-1);
    }

    @Test
    public void getIncludes_should_return_event_name_filter_with_negative_daily_limit_if_daily_limit_is_not_specified() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                        "    \"*\": {\n" +
                        "        \"includes\": [\n" +
                        "            {\n" +
                        "                \"name\" : \"yoda\"\n" + // dailyLimit missing
                        "            }\n" +
                        "        ]\n" +
                        "    }" +
                        "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0); // *
        List<Filter> includes = limit.getIncludes();
        assertThat(includes).hasSize(1);
        Filter filter = includes.get(0);
        assertThat(filter).isExactlyInstanceOf(Filter.EventNameFilter.class);
        Filter.EventNameFilter nameFilter = (Filter.EventNameFilter) filter;
        // when
        int dailyLimit = nameFilter.getDailyLimit();
        // then
        assertThat(dailyLimit).isEqualTo(-1);
    }

    @Test
    public void getIncludes_should_return_1_event_name_filter_with_ratio_and_daily_limit() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                        "    \"*\": {\n" +
                        "        \"includes\": [\n" +
                        "            {\n" +
                        "                \"name\" : \"yoda\",\n" +
                        "                \"ratio\" : \"0.544\",\n" +
                        "                \"dailyLimit\" : \"42\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }" +
                        "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0); // *
        List<Filter> includes = limit.getIncludes();
        assertThat(includes).hasSize(1);
        Filter filter = includes.get(0);
        assertThat(filter).isExactlyInstanceOf(Filter.EventNameFilter.class);
        Filter.EventNameFilter nameFilter = (Filter.EventNameFilter) filter;
        // when
        float ratio = nameFilter.getRatio();
        int dailyLimit = nameFilter.getDailyLimit();
        // then
        assertThat(ratio).isEqualTo(0.544f);
        assertThat(dailyLimit).isEqualTo(42);
    }

    @Test
    public void getIncludes_should_return_1_event_property_filter() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "    \"*\": {\n" +
                "        \"includes\": [\n" +
                "            {\n" +
                "                \"property\" : \"yoda\",\n" +
                "                \"value\" : \"jedi\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0); // *
        // when
        List<Filter> includes = limit.getIncludes();
        // then
        assertThat(includes).are(new Condition<>(
                (filter) -> filter instanceof Filter.EventPropertyFilter, "is EventPropertyFilter"));
    }

    @Test
    public void getIncludes_should_NOT_have_property_value_filter_if_value_is_missing() throws JsonProcessingException {
        // given
        String config =
                "{\n" +
                "    \"*\": {\n" +
                "        \"includes\": [\n" +
                "            {\n" +
                "                \"property\" : \"yoda\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }" +
                "}";
        List<PluginLimits> limits = PluginLimitsDeserialization.create(config);
        PluginLimits limit = limits.get(0); // *
        // when
        List<Filter> includes = limit.getIncludes();
        // then
        assertThat(includes).isEmpty();
    }
}
