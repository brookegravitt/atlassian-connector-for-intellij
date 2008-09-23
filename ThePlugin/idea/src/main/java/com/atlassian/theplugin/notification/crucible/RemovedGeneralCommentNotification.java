package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

public class RemovedGeneralCommentNotification extends AbstractReviewNotification {
	private final GeneralComment comment;

	public RemovedGeneralCommentNotification(ReviewData review, GeneralComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.DELETED_GENERAL_COMMENT;
	}

	public String getPresentationMessage() {
		return "General comment removed by " + comment.getAuthor().getDisplayName();
	}
}
