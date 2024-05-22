/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.configuration.limits;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Service
@State(name="limitsState")
@Storage(value = "limitsState.xml", roamingType = RoamingType.PER_OS)
public class LimitsPersistentState implements PersistentStateComponent<LimitsPersistentState.LimitsState> {

    private LimitsState state;

    @Override
    public @Nullable LimitsPersistentState.LimitsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull LimitsState state) {
        this.state = state;
    }

    public static class LimitsState {
        private Map<String, String> properties = new HashMap<>();

        public String get(String key) {
            return properties.get(key);
        }

        public void put(String key, String value) {
            properties.put(key, value);
        }
    }
}
