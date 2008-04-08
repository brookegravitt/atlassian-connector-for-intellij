package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.StatusListener;

import java.util.Collection;

public interface CrucibleStatusListener extends StatusListener {
	void updateReviews(Collection<ReviewDataInfo> reviews);
}