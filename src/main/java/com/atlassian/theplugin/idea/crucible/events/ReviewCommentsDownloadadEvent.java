package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

public class ReviewCommentsDownloadadEvent extends CrucibleEvent {
	private Review review;

	public ReviewCommentsDownloadadEvent(CrucibleReviewActionListener caller, Review review) {
		super(caller);
		this.review = review;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.commentsDownloaded(review);
	}
}
