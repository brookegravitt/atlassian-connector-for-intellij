package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

public class GeneralCommentReplyAdded extends CrucibleEvent {
	private ReviewData review;
	private GeneralComment parentComment;
	private GeneralComment comment;

	public GeneralCommentReplyAdded(CrucibleReviewActionListener caller, ReviewData review,
            GeneralComment parentComment, GeneralComment comment) {
		super(caller);
		this.review = review;
		this.parentComment = parentComment;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.createdGeneralCommentReply(review, parentComment, comment);
	}
}
