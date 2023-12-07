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

import com.redhat.devtools.intellij.telemetry.core.IService;
import com.redhat.devtools.intellij.telemetry.core.service.FeedbackMessageBuilder.FeedbackMessage;
import com.redhat.devtools.intellij.telemetry.core.service.FeedbackMessageBuilder.FeedbackServiceFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.redhat.devtools.intellij.telemetry.core.service.Event.Type.ACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FeedbackMessageBuilderTest {

    private final IService serviceFacadeMock = mock(IService.class);
    private final FeedbackMessageBuilder builder = new FeedbackMessageBuilder(serviceFacadeMock);
    private final IService service = mock(IService.class);
    private final TestableFeedbackServiceFacade serviceFacade = spy(new TestableFeedbackServiceFacade(service));
    private final Event event = mock(Event.class);

    @Test
    void action_should_create_message_with_action_type() {
        // given
        // when
        FeedbackMessage message = builder.feedback("azrael");
        // then
        assertThat(message.getType()).isEqualTo(ACTION);
    }

    @Test
    void action_should_create_message_with_given_name() {
        // given
        String name = "papa smurf";
        // when
        FeedbackMessage message = builder.feedback(name);
        // then
        assertThat(message.getName()).isEqualTo(name);
    }

    @Test
    void property_should_add_property_with_given_key_and_name() {
        // given
        String key = "likes";
        String value = "papa smurf";
        // when
        FeedbackMessage message = builder.feedback("smurfette").property(key, value);
        // then
        assertThat(message.getProperty(key)).isEqualTo(value);
    }

    @Test
    void property_should_ignore_property_with_null_key() {
        // given
        FeedbackMessage message = builder.feedback("smurfette");
        int beforeAdding = message.properties().size();
        // when
        message.property(null, "papa smurf");
        // then
        assertThat(message.properties()).hasSize(beforeAdding);
    }

    @Test
    void property_should_ignore_property_with_null_value() {
        // given
        FeedbackMessage message = builder.feedback("smurfette");
        int beforeAdding = message.properties().size();
        // when
        message.property("likes", null);
        // then
        assertThat(message.properties()).hasSize(beforeAdding);
    }

    @Test
    void send_should_send_message_via_service_facade() {
        // given
        FeedbackMessage message = builder.feedback("gargamel");
        // when
        message.send();
        // then
        verify(serviceFacadeMock).send(any(Event.class));
    }

    @Test
    void send_should_send_event_with_given_type_name_and_properties() {
        // given
        String name = "gargamel";
        String key1 = "the lovliest";
        String value1 = "smurfette";
        String key2 = "the smallest";
        String value2 = "baby smurf";
        FeedbackMessage message = builder.feedback(name)
                .property(key1, value1)
                .property(key2, value2);
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);
        // when
        message.send();
        // then
        verify(serviceFacadeMock).send(eventArgument.capture());
        Event event = eventArgument.getValue();
        assertThat(event.getType()).isEqualTo(ACTION);
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getProperties())
                .containsEntry(key1, value1)
                .containsEntry(key2,value2);
    }

    @Test
    void send_should_send_to_same_facade_instance() {
        // given
        FeedbackMessage message1 = builder.feedback("gargamel");
        FeedbackMessage message2 = builder.feedback("azrael");
        FeedbackMessage message3 = builder.feedback("papa smurf");
        // when
        message1.send();
        message2.send();
        message3.send();
        // then
        verify(serviceFacadeMock, times(3)).send(any(Event.class));
    }

    @Test
    void serviceFacade_send_should_create_service_once_only() {
        // given
        // when
        serviceFacade.send(event);
        serviceFacade.send(event);
        serviceFacade.send(event);
        // then
        verify(serviceFacade, times(1)).onCreated(any());
    }

    private static class TestableFeedbackServiceFacade extends FeedbackServiceFacade {

        protected TestableFeedbackServiceFacade(IService service) {
            super(() -> service);
        }

        /* public for testing purposes */
        @Override
        public void onCreated(IService value) {
            super.onCreated(value);
        }
    }

}
