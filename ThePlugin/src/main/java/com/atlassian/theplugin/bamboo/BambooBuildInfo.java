package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.RequestDataInfo;
import com.atlassian.theplugin.configuration.Server;

import java.util.Date;

public class BambooBuildInfo extends RequestDataInfo implements BambooBuild {
	private Server server;
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


	public BambooBuildInfo() {
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
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
		this.buildTime = new Date(buildTime.getTime());
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

}
