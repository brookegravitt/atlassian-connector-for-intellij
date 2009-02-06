package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ViewIssueAction extends JIRAAbstractAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		if (issue != null) {
			BrowserUtil.launchBrowser(issue.getIssueUrl());
		}
	}


	public void onUpdate(AnActionEvent event) {
	}

	public void onUpdate(AnActionEvent event, boolean enabled) {
		if (enabled) {
			event.getPresentation().setEnabled(event.getData(Constants.ISSUE_KEY) != null);
		}
	}
}
