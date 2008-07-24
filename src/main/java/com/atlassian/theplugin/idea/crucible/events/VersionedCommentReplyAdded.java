package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 11:50:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentReplyAdded extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedComment parentComment;
	private VersionedComment comment;

	public VersionedCommentReplyAdded(CrucibleReviewActionListener caller, ReviewData review, CrucibleFileInfo file,
								 VersionedComment parentComment, VersionedComment comment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.parentComment = parentComment;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.createdVersionedCommentReply(review, file, parentComment, comment);
	}
}
