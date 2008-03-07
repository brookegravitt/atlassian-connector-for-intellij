package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.RequestData;

import java.util.Date;

/**
 * Build information retrieved from Bamboo server.
 */
public interface BambooBuild extends RequestData {
	String getServerUrl();

    String getProjectName();

    String getProjectKey();

    String getProjectUrl();

	String getBuildUrl();

    String getBuildName();

	String getBuildKey();

	boolean getEnabled();

	String getBuildNumber();

    String getBuildResultUrl();

	BuildStatus getStatus();

	String getMessage();

	int getTestsPassed();

	int getTestsFailed();

	Date getBuildTime();

	String getBuildRelativeBuildDate();
}
