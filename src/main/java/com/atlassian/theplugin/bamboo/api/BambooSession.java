package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BuildDetails;
import com.atlassian.theplugin.api.RemoteApiException;
import com.atlassian.theplugin.api.RemoteApiLoginException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-03-06
 * Time: 11:16:09
 */
public interface BambooSession {
	void login(String name, char[] aPassword) throws RemoteApiLoginException;

	void logout();

	int getBamboBuildNumber() throws RemoteApiException;

	List<BambooProject> listProjectNames() throws RemoteApiException;

	List<BambooPlan> listPlanNames() throws RemoteApiException;

	BambooBuild getLatestBuildForPlan(String planKey) throws RemoteApiException;

	List<String> getFavouriteUserPlans() throws RemoteApiException;

	BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws RemoteApiException;

	void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws RemoteApiException;

	void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws RemoteApiException;

	void executeBuild(String buildKey) throws RemoteApiException;

	boolean isLoggedIn();
}
