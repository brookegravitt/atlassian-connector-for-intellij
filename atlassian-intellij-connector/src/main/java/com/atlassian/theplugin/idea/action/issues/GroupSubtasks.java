package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

/**
 * User: jgorycki
 * Date: Nov 25, 2008
 * Time: 12:48:20 PM
 */
public class GroupSubtasks extends ToggleAction {
	public boolean isSelected(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			return panel.isGroupSubtasksUnderParent();
		}
		return false;
	}

	public void setSelected(AnActionEvent e, boolean state) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.setGroupSubtasksUnderParent(state);
		}
	}
}
