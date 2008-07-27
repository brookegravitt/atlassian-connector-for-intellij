package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

public class GeneralCommentReplyAboutToAdd extends CrucibleEvent {
	private ReviewData review;
	private GeneralComment parentComment;
	private GeneralCommentBean newComment;

	public GeneralCommentReplyAboutToAdd(CrucibleReviewActionListener caller, ReviewData review,
            GeneralComment parentComment, GeneralCommentBean newComment) {
		super(caller);
		this.review = review;
		this.parentComment = parentComment;
		this.newComment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddGeneralCommentReply(review, parentComment, newComment);
	}
}
