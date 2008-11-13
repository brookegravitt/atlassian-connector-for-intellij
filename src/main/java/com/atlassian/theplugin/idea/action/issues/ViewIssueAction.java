package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ViewIssueAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.viewIssueInBrowser();
		}
	}

	public void update(AnActionEvent e) {
		super.update(e);
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		boolean enabled = panel != null && panel.haveSelectedIssue();
		e.getPresentation().setEnabled(enabled);
	}
}
