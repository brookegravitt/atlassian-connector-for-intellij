package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 8:10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnVersionedCommentEvent extends CrucibleEvent {
	private ReviewDataInfoAdapter reviewDataInfoAdapter;
	private VersionedComment versionedComment;

	public FocusOnVersionedCommentEvent(CrucibleReviewActionListener caller,
										ReviewDataInfoAdapter reviewDataInfoAdapter,
										VersionedComment versionedComment) {
		super(caller);
		this.reviewDataInfoAdapter = reviewDataInfoAdapter;
		this.versionedComment = versionedComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnVersionedComment(reviewDataInfoAdapter, versionedComment);
	}
}
