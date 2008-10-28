package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

public class ReviewCommentsDownloadadEvent extends CrucibleEvent {
	private ReviewAdapter review;

	public ReviewCommentsDownloadadEvent(CrucibleReviewActionListener caller, ReviewAdapter review) {
		super(caller);
		this.review = review;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.commentsDownloaded(review);
	}
}
