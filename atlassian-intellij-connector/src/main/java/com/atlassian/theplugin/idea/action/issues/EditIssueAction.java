package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class EditIssueAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		final JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}

	public void onUpdate(AnActionEvent event) {
	}
}
