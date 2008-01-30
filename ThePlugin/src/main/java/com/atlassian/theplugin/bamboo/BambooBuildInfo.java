package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.RequestDataInfo;

import java.util.Date;

public class BambooBuildInfo extends RequestDataInfo implements BambooBuild {
	private String serverUrl;
	private String projectName;
	private String projectKey;
	private String buildName;
	private String buildKey;
	private String buildState;
	private String buildNumber;
	private String buildReason;
	private String buildRelativeBuildDate;
	private String buildDurationDescription;
	private String buildTestSummary;
	private String buildCommitComment;
	private String message;

	private Date buildTime;

	public BambooBuildInfo() {
	}

	//todo: wywalic
	public BambooBuildInfo(
			String projectName, String buildName, String buildKey, String buildState, String buildNumber,
			String buildReason, String buildRelativeBuildDate, String buildDurationDescription, String buildTestSummary) {
		this.projectName = projectName;
		this.buildName = buildName;
		this.buildKey = buildKey;
		this.buildState = buildState;
		this.buildNumber = buildNumber;
		this.buildReason = buildReason;
		this.buildRelativeBuildDate = buildRelativeBuildDate;
		this.buildDurationDescription = buildDurationDescription;
		this.buildTestSummary = buildTestSummary;
	}

	public BambooBuildInfo(
			String projectName, String buildName, String buildKey, String buildState, String buildNumber,
			String buildReason, String buildRelativeBuildDate, String buildDurationDescription, String buildTestSummary,
			String buildCommitComment, Date lastPoolingTime) {
		this.projectName = projectName;
		this.buildName = buildName;
		this.buildKey = buildKey;
		this.buildState = buildState;
		this.buildNumber = buildNumber;
		this.buildReason = buildReason;
		this.buildRelativeBuildDate = buildRelativeBuildDate;
		this.buildDurationDescription = buildDurationDescription;
		this.buildTestSummary = buildTestSummary;
		this.buildCommitComment = buildCommitComment;
		setPollingTime(lastPoolingTime);
	}

	public String getServerUrl() {
		return this.serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getPlanUrl() {
		return this.serverUrl + "/browse/" + this.buildKey;
	}

	public String getBuildUrl() {
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

//	public String getBuildState() {
//		return buildState;
//	}

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
		if ("Successful".equalsIgnoreCase(buildState)) {
			return BuildStatus.BUILD_SUCCEED;
		} else if ("Failed".equalsIgnoreCase(buildState)) {
			return BuildStatus.BUILD_FAILED;
		} else {
			return BuildStatus.UNKNOWN;
		}
	}

	public String getMessage() {
		return this.message;
	}

	public int getTestsPassed() {
		//TODO: implement method getTestsPassed
		throw new UnsupportedOperationException("method getTestsPassed not implemented");
	}

	public int getTestsFailed() {
		//TODO: implement method getTestsFailed
		throw new UnsupportedOperationException("method getTestsFailed not implemented");
	}

	public void setBuildTime(Date buildTime) {
		this.buildTime = buildTime;
	}

	public Date getBuildTime() {
		return buildTime;
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
				+ " " + buildRelativeBuildDate
				+ " " + buildDurationDescription
				+ " " + buildTestSummary
				+ " " + buildCommitComment;
	}

}
