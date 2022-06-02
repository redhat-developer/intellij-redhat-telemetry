/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class EnvironmentTest {

    private final IDE ide = Mockito.mock(IDE.class);
    private final Plugin plugin = Mockito.mock(Plugin.class);
    private final Environment.Builder envBuilder = new Environment.Builder();

    @Test
    public void should_use_user_timezone_if_none_given() {
        // given
        String userTimezone = System.getProperty("user.timezone", "");
        Environment env = envBuilder
                .ide(ide)
                .plugin(plugin)
                .build();
        // when
        String timezone = env.getTimezone();
        // then
        assertThat(timezone).isEqualTo(userTimezone);
    }

    @Test
    public void should_use_given_timezone() {
        // given
        String givenTimezone = "America/Barbados";
        Environment env = envBuilder
                .ide(ide)
                .timezone(givenTimezone)
                .plugin(plugin)
                .build();
        // when
        String timezone = env.getTimezone();
        // then
        assertThat(timezone).isEqualTo(givenTimezone);
    }

    @Test
    public void should_use_user_timezone_country_if_none_given() {
        // given
        TimeZone defaultTimeZone = TimeZone.getTimeZone("Pacific/Palau");
        TimeZone.setDefault(defaultTimeZone);
        Environment env = envBuilder
                .ide(ide)
                .plugin(plugin)
                .build();
        // when
        String country = env.getCountry();
        // then
        assertThat(country).isEqualTo("PW");
    }

    @Test
    public void should_use_given_country() {
        // given
        String givenCountry = "CH";
        Environment env = envBuilder
                .ide(ide)
                .country(givenCountry)
                .plugin(plugin)
                .build();
        // when
        String country = env.getCountry();
        // then
        assertThat(country).isEqualTo(givenCountry);
    }

    @Test
    public void should_use_unknown_country_if_unknown_timezone() {
        // given
        Environment env = envBuilder
                .ide(ide)
                .timezone("bogus")
                .plugin(plugin)
                .build();
        // when
        String country = env.getCountry();
        // then constant
        assertThat(country).isEqualTo(Environment.UNKNOWN_COUNTRY);
    }

    @Test
    public void should_use_given_timezone_for_country() {
        // given
        TimeZone timezone = TimeZone.getTimeZone("America/New_York");
        Environment env = envBuilder
                .ide(ide)
                .timezone(timezone.getID())
                .plugin(plugin)
                .build();
        // when
        String country = env.getCountry();
        // then constant
        assertThat(country).isEqualTo("US");
    }

    @Test
    public void should_use_user_locale_if_none_given() {
        // given
        String userLocale = Locale.getDefault().toString();
        Environment env = envBuilder
                .ide(ide)
                .plugin(plugin)
                .build();
        // when
        String locale = env.getLocale();
        // then
        assertThat(locale).isEqualTo(convertLocale(userLocale));
    }

    private String convertLocale(String locale) {
        return locale.replace('_', '-');
    }

    @Test
    public void should_use_given_locale() {
        // given
        String givenLocale = "en-US";
        Environment env = envBuilder
                .ide(ide)
                .locale(givenLocale)
                .plugin(plugin)
                .build();
        // when
        String locale = env.getLocale();
        // then
        assertThat(locale).isEqualTo(givenLocale);
    }
}
