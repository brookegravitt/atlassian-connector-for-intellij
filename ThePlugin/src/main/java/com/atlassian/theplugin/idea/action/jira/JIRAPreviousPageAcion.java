package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class JIRAPreviousPageAcion extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getJIRAToolWindowPanel(event).prevPage();
	}

	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getJIRAToolWindowPanel(event) != null) {
			event.getPresentation().setEnabled(IdeaHelper.getJIRAToolWindowPanel(event).isPrevPageAvailable());
		}
	}
}
