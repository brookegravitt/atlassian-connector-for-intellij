package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public class CollapseIssuesAction extends JIRAAbstractAction {
	public void actionPerformed(final AnActionEvent e) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.collapseAllRightTreeNodes();
		}
	}

	public void onUpdate(AnActionEvent event) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		boolean enabled = panel != null && (panel.getSelectedServer() != null || panel.isRecentlyOpenFilterSelected());
		event.getPresentation().setEnabled(enabled);
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		if (ModelFreezeUpdater.getState(event)) {
			onUpdate(event);
		}
	}
}
