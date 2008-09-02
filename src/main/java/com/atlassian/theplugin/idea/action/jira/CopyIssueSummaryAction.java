package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.jira.api.JIRAIssue;

public class CopyIssueSummaryAction extends AbstractClipboardAction {
	protected String getCliboardText(final JIRAIssue issue) {
		return issue.getSummary();
	}
}