package com.atlassian.theplugin.idea.crucible.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class ReviewCompletedNotification implements CrucibleNotification {
    private Review review;

    public ReviewCompletedNotification(Review review) {
        this.review = review;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.REVIEW_COMPLETED;
    }

    public String getPresentationMessage() {
        return "Review: " + review.getPermaId().getId() + " completed";
    }
}