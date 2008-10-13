package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;

public class RemovedVersionedCommentNotification extends AbstractReviewNotification {
	private final VersionedComment comment;

	public RemovedVersionedCommentNotification(ReviewAdapter review, VersionedComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.DELETED_VERSIONED_COMMENT;
	}

	public String getPresentationMessage() {
		return "Comment removed by " + comment.getAuthor().getDisplayName();
	}
}
