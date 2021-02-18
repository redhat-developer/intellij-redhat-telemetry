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
import com.intellij.openapi.project.DumbAwareAction;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;

public class TelemetryNotifications {

    private static final NotificationGroup TELEMETRY_NOTIFICATION = new NotificationGroup(
            "Telemetry Notification Group",
            NotificationDisplayType.STICKY_BALLOON,
            true);

    public static void queryUserConsent() {
        Notification notification = TELEMETRY_NOTIFICATION.createNotification("Enable Telemetry",
                "Help Red Hat improve its extensions by allowing them to collect usage data. " +
                        "Read our <a href=\"https://developers.redhat.com/article/tool-data-collection\">privacy statement</a> " +
                        "and learn how to <a href=\"\">opt out</a>.",
                NotificationType.INFORMATION,
                new NotificationListener.UrlOpeningListener(false));
        DumbAwareAction accept = NotificationAction.create("Accept",
                e -> enableTelemetry(true, notification));
        DumbAwareAction deny = NotificationAction.create("Deny",
                e -> enableTelemetry(false, notification));
        notification
                .addAction(accept)
                .addAction(deny)
                .setIcon(AllIcons.General.TodoQuestion)
                .notify(null);

    }

    private static void enableTelemetry(boolean enable, Notification notification) {
        TelemetryConfiguration.INSTANCE.setEnabled(enable);
        notification.expire();
    }

}
