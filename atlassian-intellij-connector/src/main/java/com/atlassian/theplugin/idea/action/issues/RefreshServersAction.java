package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RefreshServersAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.refreshModels();
		}
	}

	public void onUpdate(AnActionEvent event) {
	
	}
}
