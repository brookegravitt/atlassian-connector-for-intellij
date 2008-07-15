package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 17, 2008
* Time: 8:41:47 PM
* To change this template use File | Settings | File Templates.
*/
public class ShowReviewEvent extends CrucibleEvent {
	private CrucibleChangeSet reviewInfo;

	public ShowReviewEvent(CrucibleReviewActionListener caller, CrucibleChangeSet reviewInfo) {
		super(caller);
		this.reviewInfo = reviewInfo;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showReview(reviewInfo);
	}
}
