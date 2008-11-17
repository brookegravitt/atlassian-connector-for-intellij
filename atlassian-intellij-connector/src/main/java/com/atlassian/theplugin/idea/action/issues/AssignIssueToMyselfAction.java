package com.atlassian.theplugin.idea.action.issues;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;

public class AssignIssueToMyselfAction extends AnAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
	    IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(anActionEvent);
		if (panel != null) {
			panel.assignIssueToMyself();
		}
	}
}
