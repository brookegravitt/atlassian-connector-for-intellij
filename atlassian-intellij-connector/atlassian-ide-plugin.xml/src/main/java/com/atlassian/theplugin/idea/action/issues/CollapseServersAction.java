package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CollapseServersAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.collapseAllLeftTreeNodes();
		}
	}

	public void onUpdate(AnActionEvent event) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		boolean enabled = panel != null;
		event.getPresentation().setEnabled(enabled);
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		if (ModelFreezeUpdater.getState(event)) {
			onUpdate(event);
		}
	}
}
