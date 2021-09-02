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

import java.util.TimeZone;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CountryTest {

    @Test
    void get_should_return_country_for_timezoneId() {
        // given
        // when
        String country = Country.getInstance().get(TimeZone.getTimeZone("Europe/Zurich"));
        // then
        assertThat(country).isEqualTo("CH");
    }

    @Test
    void get_should_return_null_for_null_timezoneId() {
        // given
        // when
        String country = Country.getInstance().get((String) null);
        // then
        assertThat(country).isEqualTo(null);
    }

    @Test
    void get_should_return_timezoneId_for_unknown_timezoneId() {
        // given
        // when
        TimeZone timeZone = TimeZone.getDefault();
        timeZone.setID("Aldreean/Organa Major");
        String country = Country.getInstance().get(timeZone);
        // then
        assertThat(country).isNull();
    }

    @Test
    void get_should_return_country_for_timezoneId_with_alternative() {
        // given
        // when "America/Argentina/ComodRivadavia" -> "a: America/Argentina/Catamarca" -> "c: AR" -> "Argentina"
        String country = Country.getInstance().get(TimeZone.getTimeZone("America/Argentina/ComodRivadavia"));
        // then
        assertThat(country).isEqualTo("AR");
    }

    @Test
    void get_should_return_null_for_timezoneId_without_country_nor_alternative() {
        // given
        // when
        String country = Country.getInstance().get(TimeZone.getTimeZone("CET"));
        // then
        assertThat(country).isNull();
    }

}
