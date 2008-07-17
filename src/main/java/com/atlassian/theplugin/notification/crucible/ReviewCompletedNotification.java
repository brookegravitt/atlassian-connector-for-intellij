package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.idea.crucible.ReviewData;

public class ReviewCompletedNotification extends AbstractReviewNotification {

    public ReviewCompletedNotification(ReviewData review) {
        super(review);
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.REVIEW_COMPLETED;
    }

    public String getPresentationMessage() {
        return "All reviewers completed review";
    }
}