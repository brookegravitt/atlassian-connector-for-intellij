package com.atlassian.theplugin.idea.action.jira;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class AssignIssueAndStartWorkAction extends AnAction {
	public AssignIssueAndStartWorkAction() {
		super("Assign to Me and Start Working");
	}

	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getJIRAToolWindowPanel(e).startWorkingOnIssue();
	}
}
