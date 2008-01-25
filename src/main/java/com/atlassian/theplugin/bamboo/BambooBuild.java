package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.RequestData;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:44:14 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BambooBuild extends RequestData {
	String getServerUrl();

	String getPlanUrl();

	String getBuildUrl();

	String getProjectName();

	String getProjectKey();

	String getBuildName();

	String getBuildKey();

	String getBuildState();

	String getBuildNumber();

	String getBuildReason();

	String getBuildRelativeBuildDate();

	String getBuildDurationDescription();

	String getBuildTestSummary();

	String getBuildCommitComment();

	BuildStatus getStatus();

	String getMessage();
}
