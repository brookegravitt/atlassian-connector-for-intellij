package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: Aug 5, 2008
 * Time: 3:30:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentPublished extends CrucibleEvent {
	private ReviewData review;
	private VersionedComment comment;
	private CrucibleFileInfo file;

	public VersionedCommentPublished(final CrucibleReviewActionListener caller, final ReviewData review,
			final CrucibleFileInfo file, final VersionedComment comment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.comment = comment;
	}

	protected void notify(final CrucibleReviewActionListener listener) {
		listener.publishedVersionedComment(review, file, comment);
	}
}