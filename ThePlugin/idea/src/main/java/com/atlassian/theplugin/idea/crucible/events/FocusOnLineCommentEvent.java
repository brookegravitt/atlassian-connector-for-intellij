package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 5, 2008
 * Time: 12:11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnLineCommentEvent extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedComment comment;
	private boolean openIfClosed;

	public FocusOnLineCommentEvent(final CrucibleReviewActionListener caller, final ReviewData review,
			final CrucibleFileInfo file, final VersionedComment comment, boolean openIfClosed) {
		super(caller);
		this.review = review;
		this.file = file;
		this.comment = comment;
		this.openIfClosed = openIfClosed;
	}

	protected void notify(final CrucibleReviewActionListener listener) {
		listener.focusOnLineCommentEvent(review, file, comment, openIfClosed);
	}
}
