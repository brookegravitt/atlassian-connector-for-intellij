package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class NewReviewItemNotification extends AbstractReviewNotification {
    private CrucibleFileInfo reviewItem;

    public NewReviewItemNotification(ReviewData review, CrucibleFileInfo reviewItem) {
        super(review);
        this.reviewItem = reviewItem;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_REVIEW;
    }

    public String getPresentationMessage() {
        return "New review item added " + ((reviewItem.getFileDescriptor() != null) ? reviewItem.getFileDescriptor().getName() : "");
    }
}