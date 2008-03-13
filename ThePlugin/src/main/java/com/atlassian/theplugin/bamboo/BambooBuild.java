package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.RequestData;
import com.atlassian.theplugin.configuration.Server;

import java.util.Date;

/**
 * Build information retrieved from Bamboo server.
 */
public interface BambooBuild extends RequestData {
	Server getServer();

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

	String getBuildReason();

	Date getBuildTime();

	String getBuildRelativeBuildDate();
}
