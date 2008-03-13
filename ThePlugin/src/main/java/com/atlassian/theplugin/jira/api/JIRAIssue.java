package com.atlassian.theplugin.jira.api;

import java.net.URL;

public interface JIRAIssue {
    String getServerUrl();

    String getProjectUrl();

    String getIssueUrl();

    String getKey();

    String getProjectKey();

    String getSummary();

    String getType();

    URL getTypeIconUrl();

    String getDescription();

    JIRAConstant getTypeConstant();

    String getAssignee();

    void setAssignee(String assignee);
}
