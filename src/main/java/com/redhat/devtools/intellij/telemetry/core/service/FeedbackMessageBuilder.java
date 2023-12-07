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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;

import java.util.function.Supplier;

import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.ACTION;

public class FeedbackMessageBuilder {

    private static final Logger LOGGER = Logger.getInstance(FeedbackMessageBuilder.class);

    private final IService serviceFacade;

    public FeedbackMessageBuilder(ClassLoader classLoader) {
        this(new FeedbackServiceFacade(classLoader));
    }

    FeedbackMessageBuilder(IService serviceFacade) {
        this.serviceFacade = serviceFacade;
    }

    static class FeedbackServiceFacade extends Lazy<IService> implements IService {

        protected FeedbackServiceFacade(final ClassLoader classLoader) {
            this(() -> ApplicationManager.getApplication().getService(FeedbackServiceFactory.class).create(classLoader));
        }

        protected FeedbackServiceFacade(final Supplier<IService> supplier) {
            super(supplier);
        }

        @Override
        public void send(Event event) {
            get().send(event);
        }
    }

    public FeedbackMessage feedback(String name) {
        return new FeedbackMessage(name, serviceFacade);
    }

    public static class FeedbackMessage extends Message<FeedbackMessage>{

        private FeedbackMessage(String name, IService service) {
            super(ACTION, name, service);
        }
    }
}
