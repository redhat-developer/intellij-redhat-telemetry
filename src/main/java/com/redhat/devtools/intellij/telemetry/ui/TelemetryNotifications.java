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
package com.redhat.devtools.intellij.telemetry.ui;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionGroupUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.BalloonImpl;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TelemetryNotifications {

    private static final Logger LOGGER = Logger.getInstance(TelemetryNotifications.class);

    private static final NotificationGroup QUERY_USER_CONSENT = new NotificationGroup(
            "Enable Telemetry",
            NotificationDisplayType.STICKY_BALLOON,
            true);

    private final NotificationGroup group;

    public TelemetryNotifications() {
        this(QUERY_USER_CONSENT);
    }

    public TelemetryNotifications(NotificationGroup group) {
        this.group = group;
    }

    public void queryUserConsent() {
        Notification notification = QUERY_USER_CONSENT.createNotification("Enable Telemetry",
                "Help Red Hat improve its extensions by allowing them to collect anonymous usage data. " +
                        "Read our <a href=\"https://developers.redhat.com/article/tool-data-collection\">privacy statement</a> " +
                        "and learn how to <a href=\"\">opt out</a>.",
                NotificationType.INFORMATION,
                new NotificationListener.UrlOpeningListener(false));
        DumbAwareAction accept = NotificationAction.create("Accept",
                e -> enableTelemetry(true, notification));
        DumbAwareAction deny = NotificationAction.create("Deny",
                e -> enableTelemetry(false, notification));
        DumbAwareAction later = NotificationAction.create("Later",
                e -> notification.expire());
        notification
                .addAction(accept)
                .addAction(deny)
                .addAction(later)
                .setIcon(AllIcons.General.TodoQuestion)
                .notify(null);
    }

    private static void enableTelemetry(boolean enabled, Notification notification) {
        TelemetryConfiguration configuration = TelemetryConfiguration.INSTANCE;
        configuration.setEnabled(enabled);
        try {
            configuration.save();
        } catch (IOException e) {
            LOGGER.warn("Could not save telemetry configuration.", e);
        } finally {
            notification.expire();
        }
    }
}