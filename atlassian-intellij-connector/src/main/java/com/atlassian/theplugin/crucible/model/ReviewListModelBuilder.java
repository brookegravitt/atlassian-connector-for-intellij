package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;

import java.util.Map;

public interface ReviewListModelBuilder {
	Map<CrucibleFilter, ReviewNotificationBean> getReviewsFromServer(
			final CrucibleReviewListModel crucibleReviewListModel,
			final UpdateReason updateReason,
			final long epoch) throws InterruptedException;
}
