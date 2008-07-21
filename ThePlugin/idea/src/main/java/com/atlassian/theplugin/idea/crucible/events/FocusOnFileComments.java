package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 22, 2008
 * Time: 12:23:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnFileComments extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;

	public FocusOnFileComments(CrucibleReviewActionListener caller, ReviewData review, CrucibleFileInfo file) {
		super(caller);
		this.review = review;
		this.file = file;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnFileComments(review, file);
	}
}
