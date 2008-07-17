package com.atlassian.theplugin.notification.crucible;

import java.util.List;


public interface CrucibleNotificationListener {
    void updateNotifications(List<CrucibleNotification> notifications);

    void resetState();
}
