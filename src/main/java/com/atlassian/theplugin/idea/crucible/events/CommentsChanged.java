package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

public class CommentsChanged extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;

	public CommentsChanged(CrucibleReviewActionListener caller, ReviewData review/*, CrucibleFileInfo file*/) {
		super(caller);
		this.review = review;
//		this.file = file;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.commentsChanged(review, file);
	}
}