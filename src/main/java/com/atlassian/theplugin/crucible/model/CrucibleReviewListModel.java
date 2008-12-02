package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

import java.util.Collection;

/**
 * User: jgorycki
 * Date: Dec 2, 2008
 * Time: 10:47:23 AM
 */
public interface CrucibleReviewListModel {
	Collection<ReviewAdapter> getReviews();
	void addReview(ReviewAdapter review);
	void removeReview(ReviewAdapter review);
	void removeAll();
	void addListener(CrucibleReviewListModelListener listener);
	void removeListener(CrucibleReviewListModelListener listener);
}
