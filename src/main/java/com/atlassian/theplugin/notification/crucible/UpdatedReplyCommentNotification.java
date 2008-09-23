package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;

public class UpdatedReplyCommentNotification extends AbstractReviewNotification {

	private final Comment reply;

	public UpdatedReplyCommentNotification(ReviewData review, Comment comment, Comment reply) {
		super(review);
		this.reply = reply;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_REPLY;
	}

	public String getPresentationMessage() {
		return "Reply updated by " + reply.getAuthor().getDisplayName();
	}
}
