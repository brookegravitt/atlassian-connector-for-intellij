package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.PermId;

public interface CrucibleNotification {
    CrucibleNotificationType getType();

    PermId getId();

    String getItemUrl();

    String getPresentationMessage();
}
