package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 17, 2008
* Time: 8:41:47 PM
* To change this template use File | Settings | File Templates.
*/
public class FocusOnReviewEvent extends CrucibleEvent {
	private ReviewDataInfoAdapter reviewInfo;

	public FocusOnReviewEvent(CrucibleReviewActionListener caller, ReviewDataInfoAdapter reviewInfo) {
		super(caller);
		this.reviewInfo = reviewInfo;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnReview(reviewInfo);
	}
}
