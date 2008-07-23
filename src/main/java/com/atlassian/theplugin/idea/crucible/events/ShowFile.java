package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 11:48:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShowFile extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;

	public ShowFile(CrucibleReviewActionListener caller, ReviewData review, CrucibleFileInfo file) {
		super(caller);
		this.review = review;
		this.file = file;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showFile(review, file);
	}
}
