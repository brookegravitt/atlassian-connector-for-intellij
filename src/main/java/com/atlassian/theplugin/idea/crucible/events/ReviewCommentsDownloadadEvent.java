package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

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
