package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class ReviewCommentsDownloadadEvent extends CrucibleEvent {
	private ReviewData review;

	public ReviewCommentsDownloadadEvent(CrucibleReviewActionListener caller, ReviewData review) {
		super(caller);
		this.review = review;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.commentsDownloaded(review);
	}
}
