package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 23, 2008
 * Time: 4:32:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnRevisionCommentReplyEvent extends CrucibleEvent {
	private ReviewData reviewData;
	private GeneralComment selectedComment;

	public FocusOnRevisionCommentReplyEvent(CrucibleReviewActionListener caller,
            ReviewData reviewData, GeneralComment selectedComment) {
		super(caller);
		this.reviewData = reviewData;
		this.selectedComment = selectedComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnVersionedCommentReply(reviewData, selectedComment);
	}
}
