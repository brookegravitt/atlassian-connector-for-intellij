package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

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

	public void focusOnReview(ReviewData reviewData) {
	}

	public void focusOnFile(ReviewData reviewData, CrucibleFileInfo reviewItem) {
	}

	public void focusOnGeneralComment(ReviewData reviewData, GeneralComment comment) {
	}

	public void focusOnGeneralCommentReply(ReviewData reviewData, GeneralComment comment) {
	}

	public void focusOnVersionedComment(ReviewData reviewData, CrucibleFileInfo reviewItem,
								 Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
	}

	public void focusOnVersionedCommentReply(ReviewData reviewData, GeneralComment comment) {
	}

	/**
	 * A method ivoked by a background thread when a new review needs to be shown
	 *
	 * @param reviewData
	 */
	public void showReview(ReviewData reviewData) {
	}

	/**
	 * A method ivoked by a background thread when a new file within a review needs to be shown
	 *
	 * @param reviewData
	 * @param reviewItem
	 */
	public void showReviewedFileItem(ReviewData reviewData, CrucibleFileInfo reviewItem) {
	}

	public void showGeneralComment(ReviewData reviewData, GeneralComment comment) {
	}

	public void showGeneralCommentReply(ReviewData reviewData, GeneralComment comment) {
	}

	public void showVersionedComment(ReviewData reviewData, CrucibleFileInfo reviewItem,
							  Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
	}

	public void showVersionedCommentReply(ReviewData reviewData, GeneralComment comment) {
	}
}
