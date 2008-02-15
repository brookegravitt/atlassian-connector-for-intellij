package com.atlassian.theplugin.jira.api;

public interface JIRAIssue {
    String getServerUrl();

    String getProjectUrl();

    String getIssueUrl();

    String getKey();

    String getProjectKey();

    String getSummary();

    String getType();

    String getTypeIconUrl();
}
