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

import java.util.Properties;

public abstract class AbstractConfiguration implements IConfiguration {

    private Properties properties;

    protected AbstractConfiguration() {}

    @Override
    public String get(String key) {
        return getProperties().getProperty(key);
    }

    public void put(String key, String value) {
        getProperties().put(key, value);
    }

    Properties getProperties() {
        if (properties == null) {
            this.properties = loadProperties();
        }
        return properties;
    }

    protected abstract Properties loadProperties();
}
