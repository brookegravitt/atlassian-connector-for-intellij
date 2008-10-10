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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.RequestDataInfo;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;

public class BambooBuildInfo extends RequestDataInfo implements BambooBuild {
	private BambooServerCfg server;
	private String serverUrl;
	private String projectName;
	private String projectKey;
	private String buildName;
	private String buildKey;
	private boolean enabled = true;
	private String buildState;
	private String buildNumber;
	private String buildReason;
	private String buildRelativeBuildDate;
	private String buildDurationDescription;
	private String buildTestSummary;
	private String buildCommitComment;
	private int buildTestsPassed;
	private int buildTestsFailed;
	private String message;

	private Date buildTime;
	public static final String BUILD_SUCCESSFUL = "Successful";
	public static final String BUILD_FAILED = "Failed";
	private Set<String> commiters = new HashSet<String>();


	public BambooBuildInfo() {
	}

	public BambooServerCfg getServer() {
		return server;
	}

	public void setServer(BambooServerCfg server) {
		this.server = server;
	}
		
	public String getServerUrl() {
		return this.serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getBuildUrl() {
		return this.serverUrl + "/browse/" + this.buildKey;
	}

	public String getBuildResultUrl() {
		return this.serverUrl + "/browse/" + this.buildKey + "-" + this.buildNumber;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectKey() {
		return this.projectKey;
	}

    public String getProjectUrl() {
        return this.getServerUrl() + "/browse/"
				+ (projectKey == null ? buildKey.substring(0, buildKey.indexOf("-")) : projectKey);
    }

    public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getBuildName() {
		return buildName;
	}

	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	public String getBuildKey() {
		return buildKey;
	}

	public void setBuildKey(String buildKey) {
		this.buildKey = buildKey;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		enabled = value;
	}


	public void setBuildState(String buildState) {
		this.buildState = buildState;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getBuildReason() {
		return buildReason;
	}

	public void setBuildReason(String buildReason) {
		this.buildReason = buildReason;
	}

	public String getBuildRelativeBuildDate() {
		return buildRelativeBuildDate;
	}

	public void setBuildRelativeBuildDate(String buildRelativeBuildDate) {
		this.buildRelativeBuildDate = buildRelativeBuildDate;
	}

	public String getBuildDurationDescription() {
		return buildDurationDescription;
	}

	public void setBuildDurationDescription(String buildDurationDescription) {
		this.buildDurationDescription = buildDurationDescription;
	}

	public String getBuildTestSummary() {
		return buildTestSummary;
	}

	public void setBuildTestSummary(String buildTestSummary) {
		this.buildTestSummary = buildTestSummary;
	}

	public String getBuildCommitComment() {
		return buildCommitComment;
	}

	public BuildStatus getStatus() {
		if (BUILD_SUCCESSFUL.equalsIgnoreCase(buildState)) {
			return BuildStatus.BUILD_SUCCEED;
		} else if (BUILD_FAILED.equalsIgnoreCase(buildState)) {
			return BuildStatus.BUILD_FAILED;
		} else {
			return BuildStatus.UNKNOWN;
		}
	}

	public String getMessage() {
		return this.message;
	}

	public void setBuildTestsPassed(int buildTestsPassed) {
		this.buildTestsPassed = buildTestsPassed;
	}

	public void setBuildTestsFailed(int buildTestsFailed) {
		this.buildTestsFailed = buildTestsFailed;
	}

	public int getTestsPassed() {
		return this.buildTestsPassed;
	}

	public int getTestsFailed() {
		return this.buildTestsFailed;
	}

	public void setBuildTime(Date buildTime) {
        if (buildTime != null) {
            this.buildTime = new Date(buildTime.getTime());
        }
    }

	public Date getBuildTime() {
		return buildTime != null ? new Date(this.buildTime.getTime()) : null;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setBuildCommitComment(String buildCommitComment) {
		this.buildCommitComment = buildCommitComment;
	}

	public String toString() {
		return projectName
				+ " " + buildName
				+ " " + buildKey
				+ " " + buildState
				+ " " + buildReason
				+ " " + buildTime
				+ " " + buildDurationDescription
				+ " " + buildTestSummary
				+ " " + buildCommitComment;
	}

	/**
	 * @return wheather I'm one of the commiters to that build or not
	 */
	public boolean isMyBuild() {
		if (commiters.contains(server.getUsername())) {
			return true;
		}

		return false;
	}

	/**
	 * @return list of commiters for this build
	 */
	public Set<String> getCommiters() {
		return commiters;
	}

	public void setCommiters(final Set<String> commiters) {
		if (commiters != null) {
			this.commiters = commiters;
		}
	}
}
