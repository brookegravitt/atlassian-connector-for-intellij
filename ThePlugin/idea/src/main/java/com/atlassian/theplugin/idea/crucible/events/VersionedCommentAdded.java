package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 28, 2008
 * Time: 11:24:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentAdded extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedComment comment;

	public VersionedCommentAdded(CrucibleReviewActionListener caller, ReviewData review, CrucibleFileInfo file,
			VersionedComment newComment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.comment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.createdVersionedComment(review, file, comment);
	}
}
