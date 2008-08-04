package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 4, 2008
 * Time: 4:51:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnReviewEvent extends CrucibleEvent {
	private ReviewData review;

	public FocusOnReviewEvent(final CrucibleReviewActionListener caller, final ReviewData review) {
		super(caller);
		this.review = review;
	}


	protected void notify(final CrucibleReviewActionListener listener) {
		listener.focusOnReview(review);
	}
}
