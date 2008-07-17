package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class NewVersionedCommentNotification extends AbstractReviewNotification {
    private VersionedComment comment;

    public NewVersionedCommentNotification(ReviewData review, VersionedComment comment) {
        super(review);
        this.comment = comment;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_VERSIONED_COMMENT;
    }

    public String getPresentationMessage() {
        return "New comment added by " + comment.getDisplayUser();
    }
}