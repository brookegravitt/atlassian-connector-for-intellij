package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 11:50:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnGeneralCommentReplyEvent extends CrucibleEvent {
	private ReviewData reviewData;
	private GeneralComment comment;

	public FocusOnGeneralCommentReplyEvent(CrucibleReviewActionListener caller,
            ReviewData reviewData, GeneralComment comment) {
		super(caller);
		this.reviewData = reviewData;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showGeneralCommentReply(reviewData, comment);
	}
}
