package com.atlassian.theplugin.idea.crucible.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

public class NewGeneralCommentNotification implements CrucibleNotification {
    private Review review;
    private GeneralComment comment;

    public NewGeneralCommentNotification(Review review, GeneralComment comment) {
        this.review = review;
        this.comment = comment;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_GENERAL_COMMENT;
    }

    public String getPresentationMessage() {
        return "New general comment: " + comment.getPermId().getId();
    }
}