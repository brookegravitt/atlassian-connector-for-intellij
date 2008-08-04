package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 5, 2008
 * Time: 12:07:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnVersionedCommentEvent extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedComment comment;

	public FocusOnVersionedCommentEvent(final CrucibleReviewActionListener caller, final ReviewData review,
			final CrucibleFileInfo file, final VersionedComment comment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.comment = comment;
	}

	protected void notify(final CrucibleReviewActionListener listener) {
		listener.focusOnVersionedCommentEvent(review, file, comment);
	}
}
