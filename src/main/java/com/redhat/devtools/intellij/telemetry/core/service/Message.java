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

import java.util.HashMap;
import java.util.Map;

import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymize;

abstract class Message<T extends Message<?>> {

    private static final Logger LOGGER = Logger.getInstance(Message.class);

    static final String PROP_RESULT = "result";

    public static final String RESULT_SUCCESS = "success";

    public static final String RESULT_ABORTED = "aborted";

    static final String PROP_ERROR = "error";

    private final Event.Type type;
    private final Map<String, String> properties = new HashMap<>();
    private final String name;
    private final IService service;

    protected Message(Event.Type type, String name, IService service) {
        this.name = name;
        this.type = type;
        this.service = service;
    }

    String getName() {
        return name;
    }

    Event.Type getType() {
        return type;
    }

    String getError() {
        return getProperty(PROP_ERROR);
    }

    public T error(Exception exception) {
        if (exception == null) {
            return (T) this;
        }
        return error(exception.getMessage());
    }

    public T error(String message) {
        property(PROP_ERROR, anonymize(message));
        return clearResult();
    }

    protected T clearError() {
        properties().remove(PROP_ERROR);
        return (T) this;
    }

    String getResult() {
        return getProperty(PROP_RESULT);
    }

    public T result(String result) {
        property(PROP_RESULT, result);
        return clearError();
    }

    public T success() {
        return result(RESULT_SUCCESS);
    }

    public T aborted() {
        return result(RESULT_ABORTED);
    }

    protected T clearResult() {
        properties().remove(PROP_RESULT);
        return (T) this;
    }

    public T property(String key, String value) {
        if (key == null
                || value == null) {
            LOGGER.warn("Ignored property with key: " + key + " value: " + value);
        } else {
            properties.put(key, value);
        }
        return (T) this;
    }

    String getProperty(String key) {
        return properties.get(key);
    }

    Map<String, String> properties() {
        return properties;
    }

    protected boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public Event send() {
        Event event = new Event(type, name, new HashMap<>(properties));
        service.send(event);
        return event;
    }
}