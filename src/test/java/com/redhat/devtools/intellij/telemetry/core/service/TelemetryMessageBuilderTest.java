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
package com.redhat.devtools.intellij.telemetry.core.service;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.telemetry.core.ITelemetryService;
import com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils;
import com.redhat.devtools.intellij.telemetry.core.util.TimeUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent.Type.ACTION;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryEvent.Type.STARTUP;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage.PROP_DURATION;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage.PROP_RESULT;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ShutdownMessage;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ServiceFacade;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TelemetryMessageBuilderTest {

    private ServiceFacade serviceFacadeMock = mock(ServiceFacade.class);
    private TelemetryMessageBuilder builder = new TelemetryMessageBuilder(serviceFacadeMock);

    private ITelemetryService service = mock(ITelemetryService.class);
    private MessageBusConnection bus = mock(MessageBusConnection.class);
    private TestableServiceFacade serviceFacade = spy(new TestableServiceFacade(service, bus));
    private TelemetryEvent event = new TelemetryEvent(ACTION, "smurfette collects mushrooms");

    @Test
    public void action_should_create_message_with_action_type() {
        // given
        // when
        ActionMessage message = builder.action("azrael");
        // then
        assertThat(message.getType()).isEqualTo(ACTION);
    }

    @Test
    public void action_should_create_message_with_given_name() {
        // given
        String name = "papa smurf";
        // when
        ActionMessage message = builder.action(name);
        // then
        assertThat(message.getName()).isEqualTo(name);
    }

    @Test
    public void property_should_add_property_with_given_key_and_name() {
        // given
        String key = "likes";
        String value = "papa smurf";
        // when
        ActionMessage message = builder.action("smurfette").property(key, value);
        // then
        assertThat(message.getProperty(key)).isEqualTo(value);
    }

    @Test
    public void send_should_send_message_via_service_facade() {
        // given
        ActionMessage message = builder.action("gargamel");
        // when
        message.send();
        // then
        verify(serviceFacadeMock).send(any(TelemetryEvent.class));
    }

    @Test
    public void send_should_send_event_with_given_type_name_and_properties() {
        // given
        String name = "gargamel";
        String key1 = "the lovliest";
        String value1 = "smurfette";
        String key2 = "the smallest";
        String value2 = "baby smurf";
        ActionMessage message = builder.action(name)
                .property(key1, value1)
                .property(key2, value2);
        ArgumentCaptor<TelemetryEvent> eventArgument = ArgumentCaptor.forClass(TelemetryEvent.class);
        // when
        message.send();
        // then
        verify(serviceFacadeMock).send(eventArgument.capture());
        TelemetryEvent event = eventArgument.getValue();
        assertThat(event.getType()).isEqualTo(ACTION);
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getProperties()).containsEntry(key1, value1);
        assertThat(event.getProperties()).containsEntry(key2,value2);
    }

    @Test
    public void send_should_set_duration() throws InterruptedException {
        // given
        ActionMessage message = builder.action("jolly jumper");
        // when
        TelemetryEvent event = message.send();
        // then dont override existing duration
        assertThat(TimeUtils.toDuration(event.getProperties().get(PROP_DURATION)))
                .isNotNull();
    }

    @Test
    public void send_should_NOT_set_duration_if_already_exists() throws InterruptedException {
        // given
        ActionMessage message = builder.action("jolly jumper");
        Duration existing = Duration.ofDays(7);
        message.duration(existing);
        // when
        TelemetryEvent event = message.send();
        // then dont override existing duration
        assertThat(TimeUtils.toDuration(event.getProperties().get(PROP_DURATION)))
                .isEqualTo(existing);
    }

    @Test
    public void send_should_set_result() throws InterruptedException {
        // given
        ActionMessage message = builder.action("jolly jumper");
        // when
        TelemetryEvent event = message.send();
        // then
        assertThat(event.getProperties().get(PROP_RESULT))
                .isNotNull();
    }

    @Test
    public void send_should_NOT_set_result_if_error_exists() throws InterruptedException {
        // given
        ActionMessage message = builder.action("jolly jumper");
        message.error("lost luky luke");
        // when
        TelemetryEvent event = message.send();
        // then
        assertThat(event.getProperties().get(PROP_RESULT))
                .isNull();
    }

    @Test
    public void send_should_NOT_set_result_if_result_exists() throws InterruptedException {
        // given
        ActionMessage message = builder.action("jolly jumper");
        String result = "spits like a cowboy";
        message.result(result);
        // when
        TelemetryEvent event = message.send();
        // then dont override existing result
        assertThat(event.getProperties().get(PROP_RESULT))
                .isEqualTo(result);
    }

    @Test
    public void send_should_send_to_same_facade_instance() {
        // given
        ActionMessage message1 = builder.action("gargamel");
        ActionMessage message2 = builder.action("azrael");
        ActionMessage message3 = builder.action("papa smurf");
        // when
        message1.send();
        message2.send();
        message3.send();
        // then
        verify(serviceFacadeMock, times(3)).send(any(TelemetryEvent.class));
    }

    @Test
    public void finished_should_set_duration() throws InterruptedException {
        // given
        ActionMessage message = builder.action("inspector gadget");
        final long delay = 1 * 1000;
        Thread.sleep(delay);
        // when
        message.finished();
        // then
        assertThat(TimeUtils.toDuration(message.getDuration()))
                .isGreaterThanOrEqualTo(Duration.ofMillis(delay));
    }

    @Test
    public void finished_should_set_duration_btw_given_start_and_finished_when_stop_is_new_day() {
        // given
        ActionMessage message = builder.action("inspector gadget hits the button");
        LocalDateTime started = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));
        message.started(started);
        int hours = 2;
        LocalDateTime stopped = started.plusHours(hours);
        // when local time is crossing into new day
        message.finished(stopped);
        // then
        assertThat(TimeUtils.toDuration(message.getDuration()))
                .isEqualTo(Duration.ofHours(hours));
    }

    @Test
    public void finished_should_set_duration_btw_given_start_and_finished_when_finished_is_in_new_year() {
        // given
        ActionMessage message = builder.action("the daltons break out");
        LocalDateTime started = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));
        message.started(started);
        Duration duration = Duration.of(2, ChronoUnit.HOURS).plusDays(356 + 30);
        LocalDateTime stopped = started.plus(duration);
        // when local time is next year
        message.finished(stopped);
        // then
        assertThat(TimeUtils.toDuration(message.getDuration()))
                .isEqualTo(duration);
    }

    @Test
    public void result_should_set_result_property() {
        // given
        ActionMessage message = builder.action("flinstones");
        String result = "crushed stones";
        // when
        message.result(result);
        // then
        assertThat(message.getResult())
                .isEqualTo(result);
    }

    @Test
    public void result_should_clear_error() {
        // given
        ActionMessage message = builder.action("flinstones")
                .error("went bowling");
        // when
        message.result("crushed stones");
        // then
        assertThat(message.getError())
                .isNull();
    }

    @Test
    public void error_should_set_error_property() {
        // given
        ActionMessage message = builder.action("the simpsons");
        String error = "nuclear plant emergency";
        // when
        message.error(error);
        // then
        assertThat(message.getError())
                .isEqualTo(error);
    }

    @Test
    public void error_should_NOT_NPE_for_given_null_exception() {
        // given
        ActionMessage message = builder.action("the simpsons");
        // when
        message.error((Exception) null);
        // then
        assertThat(message.getError())
                .isNull();
    }

    @Test
    public void error_should_clear_result() {
        // given
        ActionMessage message = builder.action("the simpsons")
                .result("went skateboarding");
        // when
        message.error("nuclear plant emergency");
        // then
        assertThat(message.getResult())
                .isNull();
    }

    @Test
    public void error_should_anonymize_email() {
        // given
        ActionMessage message = builder.action("the simpsons");
        String email = "bart@simpsons.com";
        // when
        message.error(email + " caused a nuclear plant emergency");
        // then
        assertThat(message.getError())
                .doesNotContain(email);
    }

    @Test
    public void error_should_anonymize_username() {
        // given
        ActionMessage message = builder.action("the simpsons");
        // when
        String username = AnonymizeUtils.USER_NAME;
        message.error(username + " caused a nuclear plant emergency");
        // then
        assertThat(message.getError())
                .doesNotContain(username);
    }

    @Test
    public void error_should_anonymize_homedir() {
        // given
        ActionMessage message = builder.action("the smurfs");
        // when
        String homedir = AnonymizeUtils.HOME_DIR;
        message.error(homedir + " is their village");
        // then
        assertThat(message.getError())
                .doesNotContain(homedir);
    }

    @Test
    public void error_should_anonymize_tmpdir() {
        // given
        ActionMessage message = builder.action("the smurfs");
        // when
        String junkyard = AnonymizeUtils.HOME_DIR;
        message.error("there's no " + junkyard + " in the smurf village");
        // then
        assertThat(message.getError())
                .doesNotContain(junkyard);
    }

    @Test
    public void error_should_anonymize_IP_address() {
        // given
        ActionMessage message = builder.action("the smurfs");
        // when
        String IP = "192.168.1.42";
        message.error("the smurf village must be kept secret, that's why their IP " + IP + " is anonymized");
        // then
        assertThat(message.getError())
                .doesNotContain(IP);
    }

    @Test
    public void serviceFacade_send_should_lazy_create_service() {
        // given
        // when
        serviceFacade.send(event);
        serviceFacade.send(event);
        serviceFacade.send(event);
        // then
        verify(serviceFacade, times(1)).createService(any());
    }

    @Test
    public void serviceFacade_send_should_send_given_event_and_startup_message() {
        // given
        ArgumentCaptor<TelemetryEvent> events = ArgumentCaptor.forClass(TelemetryEvent.class);
        // when
        serviceFacade.send(event);
        // then
        verify(serviceFacade, times(2)).send(events.capture());
        assertThat(events.getAllValues().stream()
                    .anyMatch(event -> event.getType() == STARTUP))
                .isTrue();
    }

    @Test
    public void serviceFacade_send_should_subscribe_to_AppLifeCycle() {
        // given
        // when
        serviceFacade.send(event);
        // then
        verify(bus).subscribe(eq(AppLifecycleListener.TOPIC), any());
    }

    @Test
    public void serviceFacade_send_should_register_IDE_shutdown_listener() {
        // given
        ArgumentCaptor<AppLifecycleListener> listenerArgument = ArgumentCaptor.forClass(AppLifecycleListener.class);
        serviceFacade.send(event);
        verify(bus).subscribe(any(), listenerArgument.capture());
        AppLifecycleListener listener = listenerArgument.getValue();
        // when
        listener.appWillBeClosed(false);
        // then
        verify(serviceFacade).sendShutdown();
    }

    @Test
    public void shutdownMessage_should_report_session_duration() {
        // given
        LocalDateTime startup = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 1));
        LocalDateTime shutdown = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(4, 2));
        Duration duration = Duration.between(startup, shutdown);
        // when
        ShutdownMessage message = new ShutdownMessage(startup, shutdown, serviceFacadeMock);
        // then
        assertThat(message.getSessionDuration()).isEqualTo(TimeUtils.toString(duration));
    }

    private class TestableServiceFacade extends ServiceFacade {
        private final ITelemetryService service;
        private final MessageBusConnection bus;

        protected TestableServiceFacade(ITelemetryService service, MessageBusConnection bus) {
            super(mock(ClassLoader.class));
            this.service = service;
            this.bus = bus;
        }

        @Override
        protected ITelemetryService createService(ClassLoader classLoader) {
            return service;
        }

        @Override
        protected MessageBusConnection createMessageBusConnection() {
            return bus;
        }

        @Override
        public void sendShutdown() {
        }
    }

}
