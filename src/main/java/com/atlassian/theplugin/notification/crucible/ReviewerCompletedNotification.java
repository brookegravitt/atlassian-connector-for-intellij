package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;

public class ReviewerCompletedNotification implements CrucibleNotification {
    private Review review;
    private Reviewer reviewer;

    public ReviewerCompletedNotification(Review review, Reviewer reviewer) {
        this.review = review;
        this.reviewer = reviewer;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.REVIEW_COMPLETED;
    }

    public String getPresentationMessage() {
        return "Reviewer " + reviewer.getDisplayName() + " " + (reviewer.isCompleted()? "completed" : " uncompleted") + " review " + review.getPermaId().getId();
    }
}