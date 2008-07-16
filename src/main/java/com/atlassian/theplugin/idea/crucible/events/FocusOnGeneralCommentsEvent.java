package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 1:57:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnGeneralCommentsEvent extends CrucibleEvent {
	private ReviewData changeSet;

	public FocusOnGeneralCommentsEvent(CrucibleReviewActionListener caller, ReviewData changeSet) {
		super(caller);
		this.changeSet = changeSet;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnGeneralComments(changeSet);
	}
}
