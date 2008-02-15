package com.atlassian.theplugin.jira.api;

public interface JIRAIssue
{
    public String getServerUrl();

    public String getProjectUrl();

    public String getIssueUrl();

    public String getKey();

    public String getProjectKey();

    public String getSummary();
}
