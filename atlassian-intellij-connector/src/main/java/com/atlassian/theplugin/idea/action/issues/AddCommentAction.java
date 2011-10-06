package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AddCommentAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(anActionEvent);
		if (panel != null) {
			panel.addCommentToSelectedIssue();
		}
	}

	public void onUpdate(AnActionEvent event) {
	}
}
