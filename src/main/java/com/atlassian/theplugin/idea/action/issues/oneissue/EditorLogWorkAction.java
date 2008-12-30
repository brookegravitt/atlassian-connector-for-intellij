package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueToolWindow;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 30, 2008
 * Time: 4:10:56 PM
 */
public class EditorLogWorkAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		JIRAIssue issue = IssueToolWindow.getIssue(e.getPlace());
		if (panel != null && issue != null) {
			panel.logWorkForIssue(issue);
		}
	}
}
