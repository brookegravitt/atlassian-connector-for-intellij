package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
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
		JIRAIssue issue = event.getData(Constants.ISSUE_KEY);
		if (enabled && issue != null) {
			event.getPresentation().setEnabled(true);
		} else if (ModelFreezeUpdater.getState(event) && issue != null) {
			IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
			boolean e = panel != null && (panel.getSelectedServer() != null || panel.isRecentlyOpenFilterSelected());
			event.getPresentation().setEnabled(e);
		} else {
			event.getPresentation().setEnabled(false);
		}
	}
}
