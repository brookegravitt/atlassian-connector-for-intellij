package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;

public class CreateIssueAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.createIssue();
		}
	}

	@Override
	public void update(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);

		boolean enabled = panel != null && panel.getSelectedServer() != null;
		event.getPresentation().setEnabled(enabled);
	}
}
