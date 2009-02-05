package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;


public class AssignIssueAndStartWorkAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		final JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (issue != null && panel != null) {
			panel.startWorkingOnIssue(issue);
		}
	}

	public void onUpdate(AnActionEvent event) {
	}
}
