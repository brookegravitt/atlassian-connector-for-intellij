package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public abstract class AbstractReviewNotification implements CrucibleNotification {
    protected ReviewData review;

    public AbstractReviewNotification(ReviewData review) {
        this.review = review;
    }

    public abstract CrucibleNotificationType getType();

    public PermId getId() {
        return review.getPermId();
    }

    public String getItemUrl() {
        return review.getReviewUrl();
    }

    public abstract String getPresentationMessage();
}