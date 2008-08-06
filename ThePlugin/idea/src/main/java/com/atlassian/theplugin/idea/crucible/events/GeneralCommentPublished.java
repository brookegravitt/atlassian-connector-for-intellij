package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;


public class GeneralCommentPublished extends CrucibleEvent {
	private ReviewData review;
	private GeneralComment comment;

	public GeneralCommentPublished(final CrucibleReviewActionListener caller, final ReviewData review,
			final GeneralComment comment) {
		super(caller);
		this.review = review;
		this.comment = comment;
	}

	protected void notify(final CrucibleReviewActionListener listener) {
		listener.publishedGeneralComment(review, comment);
	}
}
