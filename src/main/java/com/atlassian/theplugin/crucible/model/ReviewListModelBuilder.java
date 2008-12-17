package com.atlassian.theplugin.crucible.model;

public interface ReviewListModelBuilder {
	void getReviewsFromServer(long epoch);
}
