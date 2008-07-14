package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 23, 2008
 * Time: 4:32:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnRevisionCommentReplyEvent extends CrucibleEvent {
	private ReviewDataInfoAdapter reviewDataInfoAdapter;
	private GeneralComment selectedComment;

	public FocusOnRevisionCommentReplyEvent(CrucibleReviewActionListener caller,
            ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment selectedComment) {
		super(caller);
		this.reviewDataInfoAdapter = reviewDataInfoAdapter;
		this.selectedComment = selectedComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnVersionedCommentReply(reviewDataInfoAdapter, selectedComment);
	}
}
