package com.atlassian.theplugin.crucible;

import java.util.Collection;

public interface CrucibleStatusListener {
	void updateReviews(Collection<ReviewDataInfo> reviews);
}