package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

/**
 * User: jgorycki
 * Date: Nov 25, 2008
 * Time: 12:48:20 PM
 */
public class GroupSubtasksAction extends ToggleAction {
	public boolean isSelected(AnActionEvent e) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(e);
		if (panel != null) {
			return panel.isGroupSubtasksUnderParent();
		}
		return false;
	}

	public void setSelected(AnActionEvent e, boolean state) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(e);
		if (panel != null) {
			panel.setGroupSubtasksUnderParent(state);
		}
	}

	public void update(AnActionEvent e) {
		super.update(e);
		ModelFreezeUpdater.getState(e);
	}
}
