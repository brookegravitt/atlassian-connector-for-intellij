package com.atlassian.theplugin.idea.crucible.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class NewReviewNotification implements CrucibleNotification {
    private Review review;

    public NewReviewNotification(Review review) {
        this.review = review;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_REVIEW;
    }

    public String getPresentationMessage() {
        return "New review: " + review.getPermaId().getId();
    }
}
