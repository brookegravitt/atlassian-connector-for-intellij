package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;

public class CopyIssueKeySummaryAction extends AbstractIssueClipboardAction {
	protected String getCliboardText(final JIRAIssue issue) {
		return issue.getKey() + " - " + issue.getSummary();
	}
}
