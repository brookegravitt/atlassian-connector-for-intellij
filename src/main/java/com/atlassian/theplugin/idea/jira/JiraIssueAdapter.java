package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.jira.api.JIRAIssue;

import javax.swing.*;

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

	public Icon getStatusTypeIcon() {
		return CachedIconLoader.getIcon(issue.getStatusTypeUrl());
	}

	public String getPriority() {
		return issue.getPriority() != null ? issue.getPriority() : "";
	}
	
	public Icon getPriorityIcon() {
		return CachedIconLoader.getIcon(issue.getPriorityIconUrl());
	}

	public String getSummary() {
		return issue.getSummary();
	}

	public String getType() {
		return issue.getType();
	}

	public Icon getTypeIcon() {
		return CachedIconLoader.getIcon(issue.getTypeIconUrl());
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
