package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 10:48:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowGeneralCommentEvent extends CrucibleEvent {
	private ReviewDataInfoAdapter reviewDataInfoAdapter;
	private GeneralComment comment;

	public ShowGeneralCommentEvent(CrucibleReviewActionListener caller, ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment generalComment) {
		super(caller);
		this.reviewDataInfoAdapter = reviewDataInfoAdapter;
		this.comment = generalComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showGeneralComment(reviewDataInfoAdapter, comment);
	}
}
