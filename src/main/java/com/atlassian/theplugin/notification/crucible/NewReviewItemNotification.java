package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

public class NewReviewItemNotification implements CrucibleNotification {
    private Review review;
    private CrucibleFileInfo reviewItem;

    public NewReviewItemNotification(Review review, CrucibleFileInfo reviewItem) {
        this.review = review;
        this.reviewItem = reviewItem;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_REVIEW;
    }

    public String getPresentationMessage() {
        return "New review item: " + ((reviewItem.getFileDescriptor() != null) ? reviewItem.getFileDescriptor().getName() : "");
    }
}