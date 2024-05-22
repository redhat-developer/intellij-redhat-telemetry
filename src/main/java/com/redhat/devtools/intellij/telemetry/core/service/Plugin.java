/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.service;

import com.intellij.openapi.extensions.PluginDescriptor;
import com.sun.istack.NotNull;

public class Plugin extends Application {

    public static final class Factory {
        public Plugin create(@NotNull PluginDescriptor descriptor) {
            return create(descriptor.getName(), descriptor.getVersion(), descriptor.getPluginId().getIdString());
        }

        public Plugin create(String name, String version, String id) {
            return new Plugin(name, version, id);
        }
    }

    private final String id;

    Plugin(String name, String version, String id) {
        super(name, version);
        this.id = id;
    }

    @Override
    public Plugin property(String key, String value) {
        super.property(key, value);
        return this;
    }

    public String getId() {
        return id;
    }
}
