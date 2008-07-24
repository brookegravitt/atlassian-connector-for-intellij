package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 24, 2008
 * Time: 5:38:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentAboutToAdd extends CrucibleEvent {
	private ReviewData review;
	private GeneralCommentBean newComment;

	public GeneralCommentAboutToAdd(CrucibleReviewActionListener caller, ReviewData review, GeneralCommentBean newComment) {
		super(caller);
		this.review = review;
		this.newComment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddGeneralComment(review, newComment);
	}
}
