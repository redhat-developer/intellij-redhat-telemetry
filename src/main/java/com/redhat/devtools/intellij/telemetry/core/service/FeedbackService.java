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

import com.intellij.openapi.diagnostic.Logger;

class FeedbackService implements IService {

    private static final Logger LOGGER = Logger.getInstance(FeedbackService.class);

    protected final IMessageBroker broker;

    public FeedbackService(final IMessageBroker broker) {
        this.broker = broker;
    }

    @Override
    public void send(Event event) {
        broker.send(event);
    }

    public void dispose() {
        broker.dispose();
    }
}
