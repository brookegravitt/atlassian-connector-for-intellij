package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 16, 2008
 * Time: 10:15:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleReviewActionListener {
	public static final CrucibleReviewActionListener I_WANT_THIS_MESSAGE_BACK = null;
	/**
	 * A method ivoked by a background thread when a new review needs to be focused
	 * @param reviewDataInfoAdapter
	 */
	void focusOnReview(ReviewDataInfoAdapter reviewDataInfoAdapter);

	/**
	 * A method ivoked by a background thread when a new file within a review needs to be focused
	 * @param reviewDataInfoAdapter
	 * @param reviewItem
	 */
	void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem);

	void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment);

	void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment);

	void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem,
								 Collection<VersionedComment> versionedComments, VersionedComment versionedComment);

	void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment);
}
