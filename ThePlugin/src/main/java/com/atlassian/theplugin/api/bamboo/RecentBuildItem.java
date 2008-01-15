package com.atlassian.theplugin.api.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuildInfo;

public class RecentBuildItem implements BambooBuildInfo {
    private String projectName;
    private String buildName;
    private String buildKey;
    private String buildState;
    private String buildNumber;
    private String buildReason;
    private String buildRelativeBuildDate;
    private String buildDurationDescription;
    private String buildTestSummary;
    private String buildCommitComment;

    public RecentBuildItem(String projectName, String buildName, String buildKey, String buildState, String buildNumber, String buildReason,
        String buildRelativeBuildDate, String buildDurationDescription, String buildTestSummary)
    {
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

    public RecentBuildItem(String projectName, String buildName, String buildKey, String buildState, String buildNumber, String buildReason,
        String buildRelativeBuildDate, String buildDurationDescription, String buildTestSummary, String buildCommitComment)
    {
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
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getBuildName()
    {
        return buildName;
    }

    public void setBuildName(String buildName)
    {
        this.buildName = buildName;
    }

    public String getBuildKey()
    {
        return buildKey;
    }

    public void setBuildKey(String buildKey)
    {
        this.buildKey = buildKey;
    }

    public String getBuildState()
    {
        return buildState;
    }

    public void setBuildState(String buildState)
    {
        this.buildState = buildState;
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public String getBuildReason()
    {
        return buildReason;
    }

    public void setBuildReason(String buildReason)
    {
        this.buildReason = buildReason;
    }

    public String getBuildRelativeBuildDate()
    {
        return buildRelativeBuildDate;
    }

    public void setBuildRelativeBuildDate(String buildRelativeBuildDate)
    {
        this.buildRelativeBuildDate = buildRelativeBuildDate;
    }

    public String getBuildDurationDescription()
    {
        return buildDurationDescription;
    }

    public void setBuildDurationDescription(String buildDurationDescription)
    {
        this.buildDurationDescription = buildDurationDescription;
    }

    public String getBuildTestSummary()
    {
        return buildTestSummary;
    }

    public void setBuildTestSummary(String buildTestSummary)
    {
        this.buildTestSummary = buildTestSummary;
    }

    public String getBuildCommitComment()
    {
        return buildCommitComment;
    }

    public void setBuildCommitComment(String buildCommitComment)
    {
        this.buildCommitComment = buildCommitComment;
    }

    public String toString()
    {
        return projectName+" "+buildName+" "+buildKey+" "+buildState+" "+buildReason+" "+buildRelativeBuildDate+" "+buildDurationDescription+" "+buildTestSummary+" "+buildCommitComment;
    }
}
