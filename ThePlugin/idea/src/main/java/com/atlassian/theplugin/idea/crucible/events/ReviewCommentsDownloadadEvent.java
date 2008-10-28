package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

public class ReviewCommentsDownloadadEvent extends CrucibleEvent {
	private ReviewAdapter review;

	public ReviewCommentsDownloadadEvent(CrucibleReviewListener caller, ReviewAdapter review) {
		super(caller);
		this.review = review;
	}

	protected void notify(CrucibleReviewListener listener) {
		listener.commentsDownloaded(review);
	}
}
