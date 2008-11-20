package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class OpenIssueAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		if (issue != null) {
			IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
			if (panel != null) {
				panel.openIssue(issue);
			}
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabled(e.getData(Constants.ISSUE_KEY) != null);
	}
}
