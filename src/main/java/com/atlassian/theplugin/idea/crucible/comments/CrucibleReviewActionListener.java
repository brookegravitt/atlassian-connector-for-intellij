package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.crucible.ReviewData;

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
	public static final CrucibleReviewActionListener ANONYMOUS = null;

	/**
	 * A method ivoked by a background thread when a new review needs to be shown
	 *
	 * @param reviewData
	 */
	public void showReview(ReviewData reviewData) {
	}

	public void focusOnGeneralComments(ReviewData review) {
	}

	public void focusOnFileComments(ReviewData review, CrucibleFileInfo file) {
	}
}
