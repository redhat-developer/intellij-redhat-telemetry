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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationNamesInfo;

import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class Environment {

    public static final String UNKNOWN_COUNTRY = "ZZ";

    private final Application plugin;
    private final Application application;
    private final Platform platform;
    private final String timezone;
    private final String locale;
    private final String country;

    private Environment(Application plugin, Application application, Platform platform, String timezone, String locale, String country) {
        this.plugin = plugin;
        this.application = application;
        this.platform = platform;
        this.timezone = timezone;
        this.locale = locale;
        this.country = country;
    }

    /**
     * Returns the plugin from which Telemetry events are sent.
     */
    public Application getPlugin() {
        return plugin;
    }

    /**
     * Returns the application from which Telemetry events are sent .
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Returns the platform (or OS) from from which Telemetry events are sent.
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Returns the user timezone, eg. 'Europe/Paris'
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Returns the user locale, eg. 'en-US'
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Returns the users ISO country code, eg. 'CA' for Canada
     */
    public String getCountry() {
        return country;
    }

    public static class Builder {

        private Application application;
        private Application plugin;
        private Platform platform;
        private String timezone;
        private String locale;
        private String country;

        public Builder application(Application application) {
            this.application = application;
            return this;
        }

        private void ensureApplication() {
            if (application == null) {
                application(new Application(
                        ApplicationNamesInfo.getInstance().getFullProductName(),
                        ApplicationInfo.getInstance().getFullVersion()));
            }
        }

        public Builder platform(Platform platform) {
            this.platform = platform;
            return this;
        }

        private void ensurePlatform() {
            if (platform == null) {
                platform(new Platform());
            }
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        private void ensureTimezone() {
            if (timezone == null) {
                timezone(TimeZone.getDefault().getID());
            }
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        private void ensureLocale() {
            if (locale == null) {
                locale(Locale.getDefault().toString().replace('_', '-'));
            }
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        private void ensureCountry() {
            if (this.country == null) {
                /*
                 * We're not allowed to query 3rd party services to determine the country.
                 * Segment won't report countries for incoming requests.
                 * We thus currently dont have any better solution than use the country in the Locale.
                 */
                ensureTimezone();
                String country = Country.getInstance().get(timezone);
                if (country == null) {
                    country = UNKNOWN_COUNTRY;
                }
                country(country);
            }
        }

        public Buildable plugin(ClassLoader classLoader) {
            return plugin(createPlugin(classLoader));
        }

        public Buildable plugin(Application plugin) {
            this.plugin = plugin;
            return new Buildable();
        }

        private Application createPlugin(ClassLoader classLoader) {
            IdeaPluginDescriptor descriptor = getPluginDescriptor(classLoader);
            if (descriptor == null) {
                return null;
            }
            return new Application(descriptor.getName(), descriptor.getVersion());
        }

        private IdeaPluginDescriptor getPluginDescriptor(ClassLoader classLoader) {
            return Arrays.stream(PluginManagerCore.getPlugins())
                    .filter(descriptor -> classLoader.equals(descriptor.getPluginClassLoader()))
                    .findFirst()
                    .orElse(null);
        }

        class Buildable {
            public Environment build() {
                ensureApplication();
                ensurePlatform();
                ensureCountry();
                ensureLocale();
                ensureTimezone();
                return new Environment(plugin, application, platform, timezone, locale, country);
            }
        }
    }

}

