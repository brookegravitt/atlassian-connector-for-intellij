package com.atlassian.theplugin.idea.jira.table.renderers;

import com.atlassian.theplugin.idea.jira.JiraIcon;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;


public class IssueStatusCellRenderer extends AbstractIssueCellRenderer {
	protected JiraIcon getJiraIcon(JiraIssueAdapter adapter) {
		return adapter.getStatusInfo();
	}
}