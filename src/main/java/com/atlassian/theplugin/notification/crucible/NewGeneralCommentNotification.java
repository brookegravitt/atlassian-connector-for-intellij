package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class NewGeneralCommentNotification extends AbstractReviewNotification {
    private GeneralComment comment;

    public NewGeneralCommentNotification(ReviewData review, GeneralComment comment) {
        super(review);
        this.comment = comment;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_GENERAL_COMMENT;
    }

    public String getPresentationMessage() {
        return "New general comment for review added by " + comment.getDisplayUser();
    }
}