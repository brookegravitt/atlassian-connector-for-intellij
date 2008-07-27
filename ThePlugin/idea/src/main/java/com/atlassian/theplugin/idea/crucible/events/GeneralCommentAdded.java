package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

public class GeneralCommentAdded extends CrucibleEvent {
	private ReviewData review;
	private GeneralComment comment;

	public GeneralCommentAdded(CrucibleReviewActionListener caller, ReviewData review, GeneralComment comment) {
		super(caller);
		this.review = review;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.createdGeneralComment(review, comment);
	}
}
