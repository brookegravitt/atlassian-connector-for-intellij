package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.jira.api.JIRAIssue;

public class CopyIssueUrlAction extends AbstractClipboardAction {
	protected String getCliboardText(final JIRAIssue issue) {
		return issue.getIssueUrl();
	}
}