package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.CrucibleBottomToolWindowPanel;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 24, 2008
 * Time: 5:40:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentAdded extends CrucibleEvent {
	private ReviewData review;
	private GeneralComment comment;

	public GeneralCommentAdded(CrucibleReviewActionListener caller, ReviewData review, GeneralComment comment) {
		super(caller);
		this.review = review;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.createdGeneralComment(review, comment);
	}
}
