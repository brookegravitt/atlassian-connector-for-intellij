package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.RequestData;

import java.util.Date;

/**
 * Build information retrieved from Bamboo server.
 */
public interface BambooBuild extends RequestData {
	String getServerUrl();

	String getPlanUrl();

	String getBuildUrl();

	String getProjectName();

	String getProjectKey();

	String getBuildName();

	String getBuildKey();

	String getBuildNumber();

	BuildStatus getStatus();

	String getMessage();

	int getTestsPassed();

	int getTestsFailed();

	Date getBuildTime();

	String getBuildRelativeBuildDate();
}
