package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.crucible.api.ReviewData;

import java.util.List;

public interface ReviewDataInfo extends ReviewData {
	String getReviewUrl();

	List<String> getReviewers();

	Server getServer();
}