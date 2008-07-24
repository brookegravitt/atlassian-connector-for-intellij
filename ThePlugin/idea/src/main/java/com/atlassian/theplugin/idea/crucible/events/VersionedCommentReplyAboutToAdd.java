package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 11:23:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentReplyAboutToAdd extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedComment parentComment;
	private VersionedCommentBean newComment;

	public VersionedCommentReplyAboutToAdd(CrucibleReviewActionListener caller, ReviewData review, CrucibleFileInfo file,
									  VersionedComment parentComment, VersionedCommentBean newComment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.parentComment = parentComment;
		this.newComment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddVersionedCommentReply(review, file, parentComment, newComment);
	}
}
