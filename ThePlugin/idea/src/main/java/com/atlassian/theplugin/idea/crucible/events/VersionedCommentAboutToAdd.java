package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 28, 2008
 * Time: 7:33:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentAboutToAdd extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedCommentBean newComment;

	public VersionedCommentAboutToAdd(CrucibleReviewActionListener caller, ReviewData review,
			CrucibleFileInfo file, VersionedCommentBean newComment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.newComment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddVersionedComment(review, file, newComment);
	}
}
