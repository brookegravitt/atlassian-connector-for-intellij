package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;

public class NewReplyCommentNotification implements CrucibleNotification {
    private Review review;
    private Comment comment;
    private Comment reply;

    public NewReplyCommentNotification(Review review, Comment comment, Comment reply) {
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