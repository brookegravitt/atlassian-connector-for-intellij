package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 8:14:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShowRevisionCommentReplyEvent extends CrucibleEvent {
	private ReviewDataInfoAdapter reviewDataInfoAdapter;
	private GeneralComment comment;

	public ShowRevisionCommentReplyEvent(CrucibleReviewActionListener caller, ReviewDataInfoAdapter reviewDataInfoAdapter,
            GeneralComment comment) {
		super(caller);
		this.reviewDataInfoAdapter = reviewDataInfoAdapter;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showVersionedCommentReply(reviewDataInfoAdapter, comment);
	}
}
