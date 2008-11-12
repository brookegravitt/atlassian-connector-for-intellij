package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.jira.api.JIRAIssue;

public class CopyIssueUrlAction extends AbstractIssueClipboardAction {
	protected String getCliboardText(final JIRAIssue issue) {
		return issue.getIssueUrl();
	}
}
