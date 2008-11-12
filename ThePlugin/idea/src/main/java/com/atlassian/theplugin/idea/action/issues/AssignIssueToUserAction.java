package com.atlassian.theplugin.idea.action.issues;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.idea.IdeaHelper;

public class AssignIssueToUserAction extends AnAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(anActionEvent);
		if (panel != null) {
			panel.assignIssueToSomebody();
		}
	}
}
