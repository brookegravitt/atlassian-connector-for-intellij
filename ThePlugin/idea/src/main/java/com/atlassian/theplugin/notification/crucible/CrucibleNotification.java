package com.atlassian.theplugin.notification.crucible;

public interface CrucibleNotification {
    CrucibleNotificationType getType();

    String getPresentationMessage();
}
