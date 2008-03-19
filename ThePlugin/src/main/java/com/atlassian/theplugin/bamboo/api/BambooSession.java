package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BuildDetails;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-03-06
 * Time: 11:16:09
 */
public interface BambooSession {
	void login(String name, char[] aPassword) throws BambooLoginException;

	void logout();

	int getBamboBuildNumber() throws BambooException;

	List<BambooProject> listProjectNames() throws BambooException;

	List<BambooPlan> listPlanNames() throws BambooException;

	BambooBuild getLatestBuildForPlan(String planKey) throws BambooException;

	List<String> getFavouriteUserPlans() throws BambooException;

	BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws BambooException;

	void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws BambooException;

	void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws BambooException;

	void executeBuild(String buildKey) throws BambooException;

	boolean isLoggedIn();
}
