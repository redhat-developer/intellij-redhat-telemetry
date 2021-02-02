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
    private IConfiguration parent;

    protected AbstractConfiguration() {
        this(null);
    }

    protected AbstractConfiguration(IConfiguration parent) {
        this.parent = parent;
    }

    public String get(String key) {
        return (String) getProperties().get(key);
    }

    public void put(String key, String value) {
        getProperties().put(key, value);
    }

    public Properties getProperties() {
        if (properties == null) {
            this.properties = loadProperties(parent);
        }
        return properties;
    }

    protected abstract Properties loadProperties(IConfiguration parent);

    public IConfiguration getParent() {
        return parent;
    }
}
