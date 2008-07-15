package com.atlassian.theplugin.idea.crucible.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.State;

public class ReviewStateChangedNotification implements CrucibleNotification {
    private Review review;

    public ReviewStateChangedNotification(Review review, State oldState) {
        this.review = review;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.REVIEW_STATE_CHANGED;
    }

    public String getPresentationMessage() {
        return "Review: " + review.getPermaId().getId() + " changed state to " + review.getState();
    }
}