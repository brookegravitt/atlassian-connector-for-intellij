package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 24, 2008
 * Time: 5:19:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentReplyAboutToAdd extends CrucibleEvent {
	private ReviewData review;
	private GeneralComment parentComment;
	private GeneralCommentBean newComment;

	public GeneralCommentReplyAboutToAdd(CrucibleReviewActionListener caller, ReviewData review, GeneralComment parentComment, GeneralCommentBean newComment) {
		super(caller);
		this.review = review;
		this.parentComment = parentComment;
		this.newComment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddGeneralCommentReply(review, parentComment, newComment);
	}
}
