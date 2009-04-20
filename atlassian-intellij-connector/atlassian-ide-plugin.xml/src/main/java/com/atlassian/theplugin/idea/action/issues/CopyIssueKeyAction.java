package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.jira.api.JIRAIssue;


public class CopyIssueKeyAction extends AbstractIssueClipboardAction {
	protected String getCliboardText(final JIRAIssue issue) {
		return issue.getKey();
	}
}
