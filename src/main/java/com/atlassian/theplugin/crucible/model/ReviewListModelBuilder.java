package com.atlassian.theplugin.crucible.model;

public interface ReviewListModelBuilder {
	void getReviewsFromServer(final boolean sendNotifications);
	long getCurrentEpoch();
}
