package com.atlassian.theplugin.jira.api;

import java.net.URL;

public interface JIRAIssue {
    String getServerUrl();

    String getProjectUrl();

    String getIssueUrl();

    String getKey();

    String getProjectKey();

	String getStatus();

	URL getStatusTypeUrl();

	String getSummary();

    String getType();

    URL getTypeIconUrl();

    String getDescription();

    JIRAConstant getTypeConstant();

	JIRAConstant getStatusConstant();

	String getAssignee();

    void setAssignee(String assignee);
}
