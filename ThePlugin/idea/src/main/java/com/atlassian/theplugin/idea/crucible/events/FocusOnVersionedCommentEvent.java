package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.crucible.CrucibleFileInfo;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 23, 2008
 * Time: 4:51:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnVersionedCommentEvent extends CrucibleEvent {
	private CrucibleChangeSet crucibleChangeSet;
	private CrucibleFileInfo reviewItem;
	private Collection<VersionedComment> versionedComments;
	private VersionedComment selectedComment;

	public FocusOnVersionedCommentEvent(CrucibleReviewActionListener caller, CrucibleChangeSet crucibleChangeSet,
										CrucibleFileInfo reviewItem, Collection<VersionedComment> versionedComments,
										VersionedComment selectedComment) {
		super(caller);
		this.crucibleChangeSet = crucibleChangeSet;
		this.reviewItem = reviewItem;
		this.versionedComments = versionedComments;
		this.selectedComment = selectedComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.focusOnVersionedComment(crucibleChangeSet, reviewItem, versionedComments, selectedComment);
	}
}
