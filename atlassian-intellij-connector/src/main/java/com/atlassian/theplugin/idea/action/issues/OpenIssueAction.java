package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class OpenIssueAction extends JIRAAbstractAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		if (issue != null) {
			IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
			if (panel != null) {
				panel.openIssue(issue);
			}
		}
	}

	@Override
	public void onUpdate(AnActionEvent event) {
	}

	@Override
	public void onUpdate(AnActionEvent event, boolean enabled) {
		JIRAIssue issue = event.getData(Constants.ISSUE_KEY);
		if (enabled && issue != null) {
			event.getPresentation().setEnabled(true);
		} else if (ModelFreezeUpdater.getState(event) && issue != null) {
			IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
			boolean e = panel != null && (panel.getSelectedServer() != null || panel.isRecentlyOpenFilterSelected());
			event.getPresentation().setEnabled(e);
		} else {
			event.getPresentation().setEnabled(false);
		}
	}
}
