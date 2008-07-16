package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;


import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 23, 2008
 * Time: 4:51:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnVersionedCommentEvent extends CrucibleEvent {
	private ReviewData reviewData;
	private CrucibleFileInfo reviewItem;
	private Collection<VersionedComment> versionedComments;
	private VersionedComment selectedComment;

	public FocusOnVersionedCommentEvent(CrucibleReviewActionListener caller, ReviewData reviewData,
										CrucibleFileInfo reviewItem, Collection<VersionedComment> versionedComments,
										VersionedComment selectedComment) {
		super(caller);
		this.reviewData = reviewData;
		this.reviewItem = reviewItem;
		this.versionedComments = versionedComments;
		this.selectedComment = selectedComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnVersionedComment(reviewData, reviewItem, versionedComments, selectedComment);
	}
}
