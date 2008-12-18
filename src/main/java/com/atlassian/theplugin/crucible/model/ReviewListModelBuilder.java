package com.atlassian.theplugin.crucible.model;

public interface ReviewListModelBuilder {
	void getReviewsFromServer(final CrucibleReviewListModel crucibleReviewListModel,
							  final UpdateReason updateReason,
							  final long epoch);
}
