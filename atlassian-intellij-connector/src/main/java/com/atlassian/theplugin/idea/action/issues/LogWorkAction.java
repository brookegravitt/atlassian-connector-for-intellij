package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class LogWorkAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		final JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null && issue != null) {
			panel.logWorkForIssue(issue);
		}
	}

	public void onUpdate(AnActionEvent event) {
	}
}
