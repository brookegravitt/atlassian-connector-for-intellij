package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 22, 2008
 * Time: 12:03:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnGeneralComments extends CrucibleEvent {
	private ReviewData review;

	public FocusOnGeneralComments(CrucibleReviewActionListener caller, ReviewData review) {
		super(caller);
		this.review = review;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnGeneralComments(review);
	}
}
