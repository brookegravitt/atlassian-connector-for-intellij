package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class ReviewStateChangedNotification extends AbstractReviewNotification {
    public ReviewStateChangedNotification(ReviewData review, State oldState) {
        super(review);
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.REVIEW_STATE_CHANGED;
    }

    public String getPresentationMessage() {
        return "Review changed state to " + review.getState().value();
    }
}