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
package com.redhat.devtools.intellij.telemetry.core.configuration;

import com.redhat.devtools.intellij.telemetry.core.service.segment.SegmentConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SegmentConfigurationTest {

    private SegmentConfiguration config;

    @BeforeEach
    void beforeEach() {
        this.config = new SegmentConfiguration(getClass().getClassLoader());
    }

    @Test
    void getNormalKey_should_return_value_in_classpath_file() throws IOException {
        // given
        // when
        String writeKey = config.getNormalWriteKey();
        // then
        assertThat(writeKey).isEqualTo("SEGPROP-normal");
    }

    @Test
    void getDebugKey_should_return_value_in_classpath_file() throws IOException {
        // given
        // when
        String writeKey = config.getDebugWriteKey();
        // then
        assertThat(writeKey).isEqualTo("SEGPROP-debug");
    }

    @Test
    void getNormalKey_should_return_overriding_system_prop() throws IOException {
        // given
        String syspropWriteKey = "SYSTEMPROP-normal";
        System.setProperty("writeKey", syspropWriteKey);
        // when
        String writeKey = config.getNormalWriteKey();
        // then
        assertThat(writeKey).isEqualTo(syspropWriteKey);
    }

}
