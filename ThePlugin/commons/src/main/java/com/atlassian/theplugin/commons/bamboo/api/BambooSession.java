/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.bamboo.api;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-03-06
 * Time: 11:16:09
 */
public interface BambooSession extends ProductSession {
	int getBamboBuildNumber() throws RemoteApiException;

	List<BambooProject> listProjectNames() throws RemoteApiException;

	List<BambooPlan> listPlanNames() throws RemoteApiException;

	BambooBuild getLatestBuildForPlan(String planKey) throws RemoteApiException;

	List<String> getFavouriteUserPlans() throws RemoteApiException;

	BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws RemoteApiException;

	void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws RemoteApiException;

	void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws RemoteApiException;

	void executeBuild(String buildKey) throws RemoteApiException;	

    byte[] getBuildLogs(String buildKey, String buildNumber) throws RemoteApiException;
}
