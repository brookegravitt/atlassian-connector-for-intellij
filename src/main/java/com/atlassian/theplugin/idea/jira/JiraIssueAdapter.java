package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.jira.api.JIRAIssue;

public class JiraIssueAdapter {
	private JIRAIssue issue;
	private boolean useIconDescription;

	public JiraIssueAdapter(JIRAIssue issue, boolean useIconDescription) {
		this.issue = issue;
		this.useIconDescription = useIconDescription;
	}

	public JIRAIssue getIssue() {
		return issue;
	}

	public boolean isUseIconDescription() {
		return useIconDescription;
	}

	public String getServerUrl() {
		return issue.getServerUrl();
	}

	public String getProjectUrl() {
		return issue.getProjectUrl();
	}

	public String getIssueUrl() {
		return issue.getIssueUrl();
	}

	public String getKey() {
		return issue.getKey();
	}

	public String getProjectKey() {
		return issue.getProjectKey();
	}

	public String getStatus() {
		return issue.getStatus();
	}

	public JiraIcon getStatusInfo() {
		return new JiraIcon(issue.getStatus(), issue.getStatusTypeUrl());		
	}

	public String getPriority() {
		return issue.getPriority() != null ? issue.getPriority() : "";
	}
	
	public JiraIcon getPriorityInfo() {
		return new JiraIcon(issue.getPriority(), issue.getPriorityIconUrl());
	}

	public String getSummary() {
		return issue.getSummary();
	}

	public String getType() {
		return issue.getType();
	}

	public JiraIcon getTypeInfo() {
		return new JiraIcon(issue.getType(), issue.getTypeIconUrl());
	}

	public String getDescription() {
		return issue.getDescription();
	}

	public JIRAConstant getTypeConstant() {
		return issue.getTypeConstant();
	}

	public JIRAConstant getStatusConstant() {
		return issue.getStatusConstant();
	}

	public String getAssignee() {
		return issue.getAssignee();
	}
}
