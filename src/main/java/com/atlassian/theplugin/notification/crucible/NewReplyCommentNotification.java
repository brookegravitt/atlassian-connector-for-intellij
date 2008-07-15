package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

public class NewReplyCommentNotification implements CrucibleNotification {
    private Review review;
    private GeneralComment comment;
    private GeneralComment reply;

    public NewReplyCommentNotification(Review review, GeneralComment comment, GeneralComment reply) {
        this.review = review;
        this.comment = comment;
        this.reply = reply;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_REPLY;
    }

    public String getPresentationMessage() {
        return "New reply: " + reply.getPermId().getId();
    }
}