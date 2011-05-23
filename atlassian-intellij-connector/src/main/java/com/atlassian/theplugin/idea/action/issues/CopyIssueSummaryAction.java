package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;

public class CopyIssueSummaryAction extends AbstractIssueClipboardAction {
	protected String getCliboardText(final JiraIssueAdapter issue) {
		return issue.getSummary();
	}
}
