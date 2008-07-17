package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class ReviewerCompletedNotification extends AbstractReviewNotification {
    private Reviewer reviewer;

    public ReviewerCompletedNotification(ReviewData review, Reviewer reviewer) {
        super(review);
        this.reviewer = reviewer;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.REVIEW_COMPLETED;
    }

    public String getPresentationMessage() {
        return "Reviewer " + reviewer.getDisplayName() + " " + (reviewer.isCompleted()? "completed" : " uncompleted") + " review";
    }
}