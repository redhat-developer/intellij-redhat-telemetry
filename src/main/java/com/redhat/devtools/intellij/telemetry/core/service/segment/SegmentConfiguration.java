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
package com.redhat.devtools.intellij.telemetry.core.service.segment;

import com.redhat.devtools.intellij.telemetry.core.configuration.ClasspathConfiguration;
import com.redhat.devtools.intellij.telemetry.core.configuration.CompositeConfiguration;
import com.redhat.devtools.intellij.telemetry.core.configuration.IConfiguration;
import com.redhat.devtools.intellij.telemetry.core.configuration.SystemProperties;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.redhat.devtools.intellij.telemetry.core.configuration.ConfigurationConstants.KEY_SEGMENT_DEBUG_WRITE;
import static com.redhat.devtools.intellij.telemetry.core.configuration.ConfigurationConstants.KEY_SEGMENT_WRITE;

public class SegmentConfiguration extends CompositeConfiguration implements ISegmentConfiguration {

    private final ClasspathConfiguration consumerClasspathConfiguration;

    public SegmentConfiguration(ClassLoader classLoader) {
        this(new ClasspathConfiguration(Paths.get("/segment.properties"), classLoader));
    }

    protected SegmentConfiguration(ClasspathConfiguration consumerClasspathConfiguration) {
        this.consumerClasspathConfiguration = consumerClasspathConfiguration;
    }

    @Override
    public void put(String key, String value) {
        consumerClasspathConfiguration.put(key, value);
    }


    @Override
    public List<IConfiguration> getConfigurations() {
        return Arrays.asList(
                new SystemProperties(),
                // segment.properties in consuming plugin
                consumerClasspathConfiguration,
                // segment-defaults.properties in this plugin
                new ClasspathConfiguration(Paths.get("/segment-defaults.properties"), getClass().getClassLoader()));
    }

    @Override
    public String getSegmentNormalKey() {
        return get(KEY_SEGMENT_WRITE);
    }

    @Override
    public String getSegmentDebugKey() {
        return get(KEY_SEGMENT_DEBUG_WRITE);
    }
}
