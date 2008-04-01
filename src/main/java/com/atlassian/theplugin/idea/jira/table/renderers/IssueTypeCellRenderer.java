package com.atlassian.theplugin.idea.jira.table.renderers;

import com.atlassian.theplugin.idea.jira.JiraIcon;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;


public class IssueTypeCellRenderer extends AbstractIssueCellRenderer {
	protected JiraIcon getJiraIcon(JiraIssueAdapter adapter) {
		return adapter.getTypeInfo();
	}
}