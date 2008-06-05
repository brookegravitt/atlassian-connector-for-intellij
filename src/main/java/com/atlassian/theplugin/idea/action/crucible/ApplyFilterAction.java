package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class ApplyFilterAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {
			IdeaHelper.getCrucibleToolWindowPanel(event).applyAdvancedFilter();
		}
	}
}
