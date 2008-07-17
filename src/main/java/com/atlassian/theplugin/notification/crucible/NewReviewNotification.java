package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.idea.crucible.ReviewData;

public class NewReviewNotification extends AbstractReviewNotification {

    public NewReviewNotification(ReviewData review) {
        super(review);
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_REVIEW;
    }

    public String getPresentationMessage() {
        return review.getName();
    }
}
