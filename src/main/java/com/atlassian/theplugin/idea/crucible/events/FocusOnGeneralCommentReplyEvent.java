package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 11:50:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnGeneralCommentReplyEvent extends CrucibleEvent {
	private ReviewDataInfoAdapter reviewDataInfoAdapter;
	private GeneralComment comment;

	public FocusOnGeneralCommentReplyEvent(CrucibleReviewActionListener caller,
            ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		super(caller);
		this.reviewDataInfoAdapter = reviewDataInfoAdapter;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showGeneralCommentReply(reviewDataInfoAdapter, comment);
	}
}
