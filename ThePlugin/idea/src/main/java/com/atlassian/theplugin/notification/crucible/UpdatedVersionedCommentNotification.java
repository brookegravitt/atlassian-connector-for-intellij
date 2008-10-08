package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class UpdatedVersionedCommentNotification extends AbstractReviewNotification {

	private final VersionedComment comment;

	public UpdatedVersionedCommentNotification(Review review, VersionedComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_VERSIONED_COMMENT;
	}

	public String getPresentationMessage() {
		return "Comment updated by " + comment.getAuthor().getDisplayName();
	}
}
