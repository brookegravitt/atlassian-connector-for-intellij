package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;

import java.util.Collection;

public interface CrucibleStatusListener {
	void updateReviews(Collection<RemoteReview> reviews);
}