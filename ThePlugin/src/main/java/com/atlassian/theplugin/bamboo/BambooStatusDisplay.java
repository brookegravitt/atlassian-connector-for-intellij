package com.atlassian.theplugin.bamboo;

public interface BambooStatusDisplay {
	void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage);
}
