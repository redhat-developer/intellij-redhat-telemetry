/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.service;

import java.util.HashMap;
import java.util.Map;

public class FeedbackEvent extends Event {

    public FeedbackEvent(String name) {
        this(name, new HashMap<>());
    }

    public FeedbackEvent(String name, Map<String, String> properties) {
        super(Type.ACTION, name, properties);
    }
}
