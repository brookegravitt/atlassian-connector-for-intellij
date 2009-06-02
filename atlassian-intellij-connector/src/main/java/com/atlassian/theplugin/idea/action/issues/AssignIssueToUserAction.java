package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AssignIssueToUserAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
		final JIRAIssue issue = anActionEvent.getData(Constants.ISSUE_KEY);
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(anActionEvent);
		if (issue != null && panel != null) {
			panel.assignIssueToSomebody(issue);
		}
	}

	public void onUpdate(AnActionEvent event) {
	}
}
