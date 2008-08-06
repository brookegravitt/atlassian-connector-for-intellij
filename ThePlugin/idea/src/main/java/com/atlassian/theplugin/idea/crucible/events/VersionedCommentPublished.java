package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;


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