package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;

public class UpdatedGeneralCommentNotification extends AbstractReviewNotification {
	private final GeneralComment comment;

	public UpdatedGeneralCommentNotification(ReviewAdapter review, GeneralComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_GENERAL_COMMENT;
	}

	public String getPresentationMessage() {
		return "General comment updated by " + comment.getAuthor().getDisplayName();
	}
}
