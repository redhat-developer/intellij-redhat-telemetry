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
import com.intellij.openapi.diagnostic.Logger;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class Environment {

    public static class EnvironmentBuilder {

        private static final Logger LOGGER = Logger.getInstance(EnvironmentBuilder.class);

        private Application extension;
        private Application application;
        private Platform platform;
        private String timezone;
        private String locale;
        private String country;

        public EnvironmentBuilder extension(Application extension) {
            this.extension = extension;
            return this;
        }

        private Application getExtension() {
            if (extension == null) {
                IdeaPluginDescriptor descriptor = getCurrentDescriptor();
                if (descriptor != null) {
                    this.extension = new Application(descriptor.getName(), descriptor.getVersion());
                }
            }
            return this.extension;
        }

        private IdeaPluginDescriptor getCurrentDescriptor() {
            try {
                IdeaPluginDescriptor[] plugins = PluginManagerCore.getPlugins();
                return Arrays.stream(plugins)
                        .filter(plugin -> isCurrentClassLoader(plugin.getPluginClassLoader()))
                        .findFirst()
                        .orElse(null);
            } catch(Exception e) {
                LOGGER.warn("Could not determine current plugin.", e);
                return null;
            }
        }

        private boolean isCurrentClassLoader(ClassLoader classLoader) {
            return Objects.equals(classLoader, Thread.currentThread().getContextClassLoader());
        }

        public EnvironmentBuilder application(Application application) {
            this.application = application;
            return this;
        }

        private Application getApplication() {
            if (application == null) {
                this.application = new Application(
                        ApplicationNamesInfo.getInstance().getFullProductName(),
                        ApplicationInfo.getInstance().getFullVersion());
            }
            return this.application;
        }

        public EnvironmentBuilder platform(Platform platform) {
            this.platform = platform;
            return this;
        }

        private Platform getPlatform() {
            if (platform == null) {
                this.platform = new Platform();
            }
            return platform;
        }

        public EnvironmentBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        private String getTimezone() {
            if (timezone == null) {
                this.timezone = System.getProperty("user.timezone", "");
            }
            return timezone;
        }

        public EnvironmentBuilder locale(String locale) {
            this.locale = locale;
            return this;
        }

        private String getLocale() {
            if (locale == null) {
                this.locale = Locale.getDefault().toString();
            }
            return locale;
        }

        public EnvironmentBuilder country(String country) {
            this.country = country;
            return this;
        }

        private String getCountry() {
            if (country == null) {
                /*
                 * We're not allowed to query 3rd party services to determine the country.
                 * Segment won't report countries for incoming requests.
                 * We thus currently dont have any better solution than use the country in the Locale.
                 */
                this.country = Locale.getDefault().getCountry();
            }
            return this.country;
        }

        public Environment build() {
            return new Environment(
                    getExtension(),
                    getApplication(),
                    getPlatform(),
                    getTimezone(),
                    getLocale(),
                    getCountry());
        }
    }

    private Application extension;
    private Application application;
    private Platform platform;
    private String timezone;
    private String locale;
    private String country;

    Environment(Application extension, Application application, Platform platform, String timezone, String locale, String country) {
        this.extension = extension;
        this.application = application;
        this.platform = platform;
        this.timezone = timezone;
        this.locale = locale;
        this.country = country;
    }

    /**
     * Returns the extension from which Telemetry events are sent.
     */
    public Application getExtension() {
        return extension;
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
}
