package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 16, 2008
 * Time: 10:15:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleReviewActionListener {
	/**
	 * A method ivoked by a background thread when a new review needs to be focused
	 * @param reviewItem
	 */
	void focusOnReview(ReviewDataInfoAdapter reviewItem);

	/**
	 * A method ivoked by a background thread when a new file within a review needs to be focused
	 * @param reviewDataInfoAdapter
	 * @param reviewItem
	 */
	void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem);
}
