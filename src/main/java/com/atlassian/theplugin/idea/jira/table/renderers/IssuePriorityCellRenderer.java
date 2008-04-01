package com.atlassian.theplugin.idea.jira.table.renderers;

import com.atlassian.theplugin.idea.jira.JiraIcon;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;


public class IssuePriorityCellRenderer extends AbstractIssueCellRenderer {
	protected JiraIcon getJiraIcon(JiraIssueAdapter adapter) {
		return adapter.getPriorityInfo();
	}
}