package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;

public class RemovedReplyCommentNotification extends AbstractReviewNotification {
	private final Comment reply;

	public RemovedReplyCommentNotification(ReviewDataImpl review, Comment reply) {
		super(review);
		this.reply = reply;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.DELETED_REPLY;
	}

	public String getPresentationMessage() {
		return "Reply removed by " + reply.getAuthor().getDisplayName();
	}
}
