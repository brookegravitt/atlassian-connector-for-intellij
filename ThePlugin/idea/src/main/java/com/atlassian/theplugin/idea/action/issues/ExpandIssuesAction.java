package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public class ExpandIssuesAction extends AnAction {
	public void actionPerformed(final AnActionEvent e) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		panel.expandAllIssueTreeNodes();
	}
}
