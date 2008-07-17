package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class NewReplyCommentNotification extends AbstractReviewNotification {
    private Comment comment;
    private Comment reply;

    public NewReplyCommentNotification(ReviewData review, Comment comment, Comment reply) {
        super(review);
        this.comment = comment;
        this.reply = reply;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_REPLY;
    }

    public String getPresentationMessage() {
        return "New reply added by " + reply.getDisplayUser();
    }
}