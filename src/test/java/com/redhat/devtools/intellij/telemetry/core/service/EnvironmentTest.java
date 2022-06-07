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

import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class EnvironmentTest {

    private final IDE ide = mock(IDE.class);
    private final Plugin plugin = mock(Plugin.class);
    private final Platform platform = mock(Platform.class);
    private final Environment.Builder envBuilder = new Environment.Builder();

    @Test
    void should_use_user_timezone_if_none_given() {
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
    void should_use_given_timezone() {
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
    void should_use_user_timezone_country_if_none_given() {
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
    void should_use_given_country() {
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
    void should_use_unknown_country_if_unknown_timezone() {
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
    void should_use_given_timezone_for_country() {
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
    void should_use_user_locale_if_none_given() {
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
    void should_use_given_locale() {
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

    @Test
    void equals_should_be_true_if_all_same() {
        // given
        Environment environment = envBuilder
                .timezone("GMT+02:00")
                .locale("de-CH")
                .country("CH")
                .platform(platform)
                .ide(ide)
                .plugin(plugin)
                .build();
        Environment equalEnvironment = envBuilder
                .timezone("GMT+02:00")
                .locale("de-CH")
                .country("CH")
                .platform(platform)
                .ide(ide)
                .plugin(plugin)
                .build();
        // when
        boolean equal = environment.equals(equalEnvironment);
        // then
        assertThat(equal).isTrue();
    }

    @Test
    void equals_should_be_false_if_timezone_is_different() {
        // given
        Environment usTimezoneEnv = envBuilder
                .timezone("GMT-08:00")
                .ide(ide)
                .plugin(plugin)
                .build();
        Environment centralEuropeTimezoneEnv = envBuilder
                .timezone("GMT+02:00")
                .ide(ide)
                .plugin(plugin)
                .build();
        // when
        boolean equal = usTimezoneEnv.equals(centralEuropeTimezoneEnv);
        // then
        assertThat(equal).isFalse();
    }

    @Test
    void equals_should_be_false_if_locale_is_different() {
        // given
        Environment enUsEnv = envBuilder
                .locale("en-US")
                .ide(ide)
                .plugin(plugin)
                .build();
        Environment deCHEnv = envBuilder
                .locale("de-CH")
                .ide(ide)
                .plugin(plugin)
                .build();
        // when
        boolean equal = enUsEnv.equals(deCHEnv);
        // then
        assertThat(equal).isFalse();
    }

    @Test
    void equals_should_be_false_if_country_is_different() {
        // given
        Environment usEnv = envBuilder
                .country("United States")
                .ide(ide)
                .plugin(plugin)
                .build();
        Environment chEnv = envBuilder
                .country("Switzerland")
                .ide(ide)
                .plugin(plugin)
                .build();
        // when
        boolean equal = usEnv.equals(chEnv);
        // then
        assertThat(equal).isFalse();
    }

    @Test
    void equals_should_be_false_if_ide_appname_is_different() {
        // given
        Environment yodaIde = envBuilder
                .ide(new IDE("yoda", "42"))
                .plugin(plugin)
                .build();
        Environment skywalkerIde = envBuilder
                .ide(new IDE("skywalker", "42"))
                .plugin(plugin)
                .build();
        // when
        boolean equal = yodaIde.equals(skywalkerIde);
        // then
        assertThat(equal).isFalse();
    }

    @Test
    void equals_should_be_false_if_ide_version_is_different() {
        // given
        Environment ide42 = envBuilder
                .ide(new IDE("yoda", "42"))
                .plugin(plugin)
                .build();
        Environment ide84 = envBuilder
                .ide(new IDE("yoda", "84"))
                .plugin(plugin)
                .build();
        // when
        boolean equal = ide42.equals(ide84);
        // then
        assertThat(equal).isFalse();
    }

    @Test
    void equals_should_be_false_if_ide_javaversion_is_different() {
        // given
        IDE java8Ide = new IDE("yoda", "42").setJavaVersion("8");
        Environment java8Env = envBuilder
                .ide(java8Ide)
                .plugin(plugin)
                .build();
        IDE java11Ide = new IDE("yoda", "42").setJavaVersion("11");
        Environment java11Env = envBuilder
                .ide(java11Ide)
                .plugin(plugin)
                .build();
        // when
        boolean equal = java8Env.equals(java11Env);
        // then
        assertThat(equal).isFalse();
    }

    @Test
    void equals_should_be_false_if_plugin_name_is_different() {
        // given
        Plugin jediPlugin = new Plugin.Factory().create("jedi", "42");
        Environment jediEnv = envBuilder
                .ide(ide)
                .plugin(jediPlugin)
                .build();
        Plugin sithPlugin = new Plugin.Factory().create("sith", "42");
        Environment sithEnv = envBuilder
                .ide(ide)
                .plugin(sithPlugin)
                .build();
        // when
        boolean equal = jediEnv.equals(sithEnv);
        // then
        assertThat(equal).isFalse();
    }

    @Test
    void equals_should_be_false_if_plugin_version_is_different() {
        // given
        Plugin jedi42Plugin = new Plugin.Factory().create("jedi", "42");
        Environment jedi42Env = envBuilder
                .ide(ide)
                .plugin(jedi42Plugin)
                .build();
        Plugin jedi84Plugin = new Plugin.Factory().create("jedi", "84");
        Environment jedi84Env = envBuilder
                .ide(ide)
                .plugin(jedi84Plugin)
                .build();
        // when
        boolean equal = jedi42Env.equals(jedi84Env);
        // then
        assertThat(equal).isFalse();
    }

}
