package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.crucible.CrucibleFileInfo;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 16, 2008
 * Time: 10:15:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleReviewActionListener {
	public static final CrucibleReviewActionListener I_WANT_THIS_MESSAGE_BACK = null;
	public static final CrucibleReviewActionListener I_DONT_CARE = null;

	public void focusOnReview(CrucibleChangeSet crucibleChangeSet) {
	}

	public void focusOnFile(CrucibleChangeSet crucibleChangeSet, CrucibleFileInfo reviewItem) {
	}

	public void focusOnGeneralComment(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
	}

	public void focusOnGeneralCommentReply(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
	}

	public void focusOnVersionedComment(CrucibleChangeSet crucibleChangeSet, CrucibleFileInfo reviewItem,
								 Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
	}

	public void focusOnVersionedCommentReply(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
	}

	/**
	 * A method ivoked by a background thread when a new review needs to be shown
	 *
	 * @param crucibleChangeSet
	 */
	public void showReview(CrucibleChangeSet crucibleChangeSet) {
	}

	/**
	 * A method ivoked by a background thread when a new file within a review needs to be shown
	 *
	 * @param crucibleChangeSet
	 * @param reviewItem
	 */
	public void showReviewedFileItem(CrucibleChangeSet crucibleChangeSet, CrucibleFileInfo reviewItem) {
	}

	public void showGeneralComment(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
	}

	public void showGeneralCommentReply(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
	}

	public void showVersionedComment(CrucibleChangeSet crucibleChangeSet, CrucibleFileInfo reviewItem,
							  Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
	}

	public void showVersionedCommentReply(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
	}
}
