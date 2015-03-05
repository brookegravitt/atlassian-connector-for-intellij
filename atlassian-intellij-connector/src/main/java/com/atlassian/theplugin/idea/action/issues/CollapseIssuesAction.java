package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public class CollapseIssuesAction extends JIRAAbstractAction {
	public void actionPerformed(final AnActionEvent e) {
		final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(e);
		if (panel != null) {
			panel.collapseAllRightTreeNodes();
		}
	}

	public void onUpdate(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
		boolean enabled = panel != null && (panel.getSelectedServer() != null || panel.isRecentlyOpenFilterSelected());
		event.getPresentation().setEnabled(enabled);
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		if (ModelFreezeUpdater.getState(event)) {
			onUpdate(event);
		}
	}
}
