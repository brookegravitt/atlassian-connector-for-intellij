package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.jira.api.JIRAIssue;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JiraIssueAdapter {
	private JIRAIssue issue;
	private boolean useIconDescription;

	private static Map<String, Icon> icons = new HashMap<String, Icon>();

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

	public URL getStatusTypeUrl() {
		return issue.getStatusTypeUrl();
	}

	public Icon getStatusTypeIcon() {
		String key = issue.getStatusTypeUrl().toString();
		if (!icons.containsKey(key)) {
			if (issue.getStatusTypeUrl() != null) {
				icons.put(key, new ImageIcon(issue.getStatusTypeUrl()));
			}
		}
		return icons.get(key);
	}

	public String getSummary() {
		return issue.getSummary();
	}

	public String getType() {
		return issue.getType();
	}

	public URL getTypeIconUrl() {
		return issue.getTypeIconUrl();
	}

	public Icon getTypeIcon() {
		String key = issue.getTypeIconUrl().toString();
		if (!icons.containsKey(key)) {
			if (issue.getTypeIconUrl() != null) {
				icons.put(key, new ImageIcon(issue.getTypeIconUrl()));
			}
		}
		return icons.get(key);
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
