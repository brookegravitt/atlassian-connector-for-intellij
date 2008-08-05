package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;


public class VersionedCommentAboutToPublish extends CrucibleEvent {
	private ReviewData review;
	private VersionedComment comment;
	private CrucibleFileInfo file;

	public VersionedCommentAboutToPublish(CrucibleReviewActionListener caller, ReviewData review,
			CrucibleFileInfo file, VersionedComment comment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.comment = comment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToPublishVersionedComment(review, file, comment);
	}
}