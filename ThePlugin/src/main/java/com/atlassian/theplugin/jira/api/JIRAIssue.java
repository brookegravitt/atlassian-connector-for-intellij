package com.atlassian.theplugin.jira.api;

public interface JIRAIssue {
    String getServerUrl();

    String getProjectUrl();

    String getIssueUrl();

    String getKey();

    String getProjectKey();

	String getStatus();

	String getStatusTypeUrl();

	String getSummary();

    String getType();

    String getTypeIconUrl();

	String getPriority();

	String getPriorityIconUrl();

	String getDescription();

    JIRAConstant getTypeConstant();

	JIRAConstant getStatusConstant();

	String getAssignee();

    void setAssignee(String assignee);
}
