package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

/**
 * User: jgorycki
 * Date: Dec 2, 2008
 * Time: 10:48:02 AM
 */
public interface CrucibleReviewListModelListener {
	void reviewAdded(ReviewAdapter review);
	void reviewRemoved(ReviewAdapter review);
	void reviewChanged(ReviewAdapter review);

	void reviewListUpdateStarted(ServerId serverId);

	void reviewListUpdateFinished(ServerId serverId);

	void reviewChangedWithoutFiles(ReviewAdapter review);
}
