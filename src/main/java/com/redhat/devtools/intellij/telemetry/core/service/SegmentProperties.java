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

import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.AnalyticsFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SegmentProperties {

    private static final Logger LOGGER = Logger.getInstance(AnalyticsFactory.class);

    public static final SegmentProperties INSTANCE = new SegmentProperties();

    private static final String TELEMETRY_SEGMENT_PROPERTIES = "/segment.properties";
    private static final String PROPERTY_WRITE_KEY = "writeKey";

    private Properties properties;

    private SegmentProperties() {
    }

    public String getWriteKey() {
        try {
            return getValue(PROPERTY_WRITE_KEY);
        } catch (IOException e) {
            LOGGER.warn("Could not load segment telemetry properties file " + TELEMETRY_SEGMENT_PROPERTIES, e);
            return null;
        }
    }

    private String getValue(String key) throws IOException {
        Properties properties = getProperties();
        if (properties == null) {
            LOGGER.warn("Could not load telemetry property value " + key);
            return null;
        }
        return (String) properties.get(key);
    }

    private Properties getProperties() throws IOException {
        if (properties == null) {
            this.properties = new Properties();
            InputStream in = getClass().getResourceAsStream(TELEMETRY_SEGMENT_PROPERTIES);
            if (in == null) {
                LOGGER.warn("Could not load segment telemetry properties file " + TELEMETRY_SEGMENT_PROPERTIES + ": not on classpath.");
                return null;
            }
            properties.load(in);
        }
        return properties;
    }
}
