package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

public class ReviewCommentsDownloadadEvent extends CrucibleEvent {
	private ReviewDataImpl review;

	public ReviewCommentsDownloadadEvent(CrucibleReviewActionListener caller, ReviewDataImpl review) {
		super(caller);
		this.review = review;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.commentsDownloaded(review);
	}
}
