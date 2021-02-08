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

import static com.redhat.devtools.intellij.telemetry.core.configuration.ConfigurationConstants.*;

public class SystemProperties extends AbstractConfiguration {

	public SystemProperties(IConfiguration parent) {
		super(parent);
	}

	@Override
	protected Properties loadProperties(IConfiguration parent) {
		Properties parentProperties = (parent == null? null : parent.getProperties());
		Properties properties = new Properties(parentProperties);
		copySystemProperty(KEY_SEGMENT_WRITE, properties);
		copySystemProperty(KEY_SEGMENT_DEBUG_WRITE, properties);
		copySystemProperty(KEY_MODE, properties);
		return properties;
	}

	private void copySystemProperty(String key, Properties properties) {
		Object value = System.getProperty(key);
		if (value != null) {
			properties.put(key, value);
		}
	}
}
