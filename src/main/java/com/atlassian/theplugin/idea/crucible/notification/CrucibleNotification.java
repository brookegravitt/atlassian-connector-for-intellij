package com.atlassian.theplugin.idea.crucible.notification;

public interface CrucibleNotification {
    CrucibleNotificationType getType();

    String getPresentationMessage();
}
