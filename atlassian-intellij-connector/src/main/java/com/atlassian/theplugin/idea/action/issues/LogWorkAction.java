package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class LogWorkAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		final JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(e);
		if (panel != null && issue != null) {
			panel.logWorkOrDeactivateIssue(issue, issue.getServer(), null, false, null);
		}
	}

	public void onUpdate(AnActionEvent event) {
	}
}
