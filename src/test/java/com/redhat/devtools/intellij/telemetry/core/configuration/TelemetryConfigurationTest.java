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

import com.redhat.devtools.intellij.telemetry.core.util.Lazy;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.*;
import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.ConfigurationChangedListener;
import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.KEY_MODE;
import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.Mode.DISABLED;
import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.Mode.NORMAL;
import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.Mode.DEBUG;
import static com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration.Mode.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TelemetryConfigurationTest {

    private SaveableFileConfiguration file = configuration(new Properties(), SaveableFileConfiguration.class);
    private IConfiguration defaults = mock(IConfiguration.class);
    private IConfiguration overrides = mock(IConfiguration.class);
    private List<IConfiguration> configurations = Arrays.asList(overrides, file, defaults);
    private ConfigurationChangedListener listener = mock(ConfigurationChangedListener.class);
    private Lazy<ConfigurationChangedListener> notifier = new Lazy<>(() -> listener);
    private TelemetryConfiguration config = new TelemetryConfiguration(file, configurations, notifier);

    @Test
    public void get_should_return_overridden_value() {
        // given
        doReturnValues(KEY_MODE,
                DEBUG.toString(), NORMAL.toString(), UNKNOWN.toString());
        // when
        String mode = config.get(KEY_MODE);
        // then
        assertThat(mode).isEqualTo(DEBUG.toString());
    }

    @Test
    public void get_should_return_file_value_if_no_overridden_value_exists() {
        // given
        doReturnValues(KEY_MODE,
                null, NORMAL.toString(), null);
        // when
        String mode = config.get(KEY_MODE);
        // then
        assertThat(mode).isEqualTo(NORMAL.toString());
    }

    @Test
    public void get_should_return_default_value_if_no_values_exist() {
        // given
        doReturnValues(KEY_MODE,
                null, null, DEBUG.toString());
        // when
        String mode = config.get(KEY_MODE);
        // then
        assertThat(mode).isEqualTo(DEBUG.toString());
    }

    @Test
    public void put_should_put_to_file() {
        // given
        String value = "red pill";
        // when
        config.put(KEY_MODE, value);
        // then
        verify(file).put(KEY_MODE, value);
        verify(overrides, never()).put(KEY_MODE, value);
        verify(defaults, never()).put(KEY_MODE, value);
    }

    @Test
    public void put_should_notify() {
        // given
        String value = "red pill";
        // when
        config.put(KEY_MODE, value);
        // then
        verify(listener).configurationChanged(KEY_MODE, value);
    }

    @Test
    public void save_should_save_file() throws IOException {
        // given
        // when
        config.save();
        // then
        verify(file).save();
    }

    @Test
    public void getMode_should_return_UNKNOWN_for_unknown_value() {
        // given
        doReturnValues(KEY_MODE,
                null, "bogus", null);
        // when
        Mode mode = config.getMode();
        // then
        assertThat(mode).isEqualTo(UNKNOWN);
    }

    @Test
    public void getMode_should_return_UNKNOWN_for_null_value() {
        // given
        doReturnValues(KEY_MODE,
                null, null, null);
        // when
        Mode mode = config.getMode();
        // then
        assertThat(mode).isEqualTo(UNKNOWN);
    }

    @Test
    public void getMode_should_return_DEBUG_for_debug_value() {
        // given
        doReturnValues(KEY_MODE,
                null, "debug", null);
        // when
        Mode mode = config.getMode();
        // then
        assertThat(mode).isEqualTo(DEBUG);
    }

    @Test
    public void getMode_should_return_NORMAL_for_normal_value() {
        // given
        doReturnValues(KEY_MODE,
                null, "normal", null);
        // when
        Mode mode = config.getMode();
        // then
        assertThat(mode).isEqualTo(NORMAL);
    }

    @Test
    public void getMode_should_NOT_be_case_sensitive() {
        // given
        doReturnValues(KEY_MODE,
                null, "dEbUg", null);
        // when
        Mode mode = config.getMode();
        // then
        assertThat(mode).isEqualTo(DEBUG);
    }

    @Test
    public void isEnabled_should_return_true_for_normal_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "normal", null);
        // when
        boolean enabled = config.isEnabled();
        // then
        assertThat(enabled).isTrue();
    }

    @Test
    public void setEnabled_true_should_set_normal_mode() {
        // given
        // when
        config.setEnabled(true);
        // then
        verify(file).put(KEY_MODE, NORMAL.toString());
    }

    @Test
    public void setEnabled_false_should_set_disabled_mode() {
        // given
        // when
        config.setEnabled(false);
        // then
        verify(file).put(KEY_MODE, DISABLED.toString());
    }

    @Test
    public void isEnabled_should_return_true_for_debug_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "debug", null);
        // when
        boolean enabled = config.isEnabled();
        // then
        assertThat(enabled).isTrue();
    }

    @Test
    public void isEnabled_should_return_false_for_disabled_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "disabled", null);
        // when
        boolean enabled = config.isEnabled();
        // then
        assertThat(enabled).isFalse();
    }

    @Test
    public void isEnabled_should_return_false_for_unknown_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "bogus", null);
        // when
        boolean enabled = config.isEnabled();
        // then
        assertThat(enabled).isFalse();
    }

    @Test
    public void isConfigured_should_return_true_for_normal_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "normal", null);
        // when
        boolean configured = config.isConfigured();
        // then
        assertThat(configured).isTrue();
    }

    @Test
    public void isConfigured_should_return_true_for_debug_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "debug", null);
        // when
        boolean configured = config.isConfigured();
        // then
        assertThat(configured).isTrue();
    }

    @Test
    public void isConfigured_should_return_true_for_disabled_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "disabled", null);
        // when
        boolean configured = config.isConfigured();
        // then
        assertThat(configured).isTrue();
    }

    @Test
    public void isConfigured_should_return_false_for_unknown_mode() {
        // given
        doReturnValues(KEY_MODE,
                null, "bogus", null);
        // when
        boolean configured = config.isConfigured();
        // then
        assertThat(configured).isFalse();
    }

    private <T extends AbstractConfiguration> T configuration(Properties properties, Class<T> clazz) {
        T mock = mock(clazz);
        doReturn(properties)
                .when(mock).loadProperties();
        return mock;
    }

    private void doReturnValues(String key, String overridesValue, String fileValue, String defaultsValue) {
        doReturn(overridesValue)
                .when(overrides).get(key);
        doReturn(fileValue)
                .when(file).get(key);
        doReturn(defaultsValue)
                .when(defaults).get(key);

    }

}
