package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BuildDetails;
import com.atlassian.theplugin.rest.RestException;
import com.atlassian.theplugin.rest.RestLoginException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-03-06
 * Time: 11:16:09
 */
public interface BambooSession {
	void login(String name, char[] aPassword) throws RestLoginException;

	void logout();

	int getBamboBuildNumber() throws RestException;

	List<BambooProject> listProjectNames() throws RestException;

	List<BambooPlan> listPlanNames() throws RestException;

	BambooBuild getLatestBuildForPlan(String planKey) throws RestException;

	List<String> getFavouriteUserPlans() throws RestException;

	BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws RestException;

	void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws RestException;

	void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws RestException;

	void executeBuild(String buildKey) throws RestException;

	boolean isLoggedIn();
}
