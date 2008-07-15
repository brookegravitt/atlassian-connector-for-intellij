package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 8:14:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShowRevisionCommentReplyEvent extends CrucibleEvent {
	private ReviewData reviewData;
	private GeneralComment comment;

	public ShowRevisionCommentReplyEvent(CrucibleReviewActionListener caller, ReviewData reviewData,
            GeneralComment comment) {
		super(caller);
		this.reviewData = reviewData;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showVersionedCommentReply(reviewData, comment);
	}
}
