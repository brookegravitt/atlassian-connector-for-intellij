package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RefreshServersAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(e);
		if (panel != null) {
			panel.refreshModels();
		}
	}

	public void onUpdate(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
		boolean enabled = panel != null;
		event.getPresentation().setEnabled(enabled);
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		if (ModelFreezeUpdater.getState(event)) {
			onUpdate(event);
		}
	}
}
