package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

public class NewVersionedCommentNotification implements CrucibleNotification {
    private Review review;
    private VersionedComment comment;

    public NewVersionedCommentNotification(Review review, VersionedComment comment) {
        this.review = review;
        this.comment = comment;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_VERSIONED_COMMENT;
    }

    public String getPresentationMessage() {
        return "New general comment: " + comment.getPermId().getId();
    }
}