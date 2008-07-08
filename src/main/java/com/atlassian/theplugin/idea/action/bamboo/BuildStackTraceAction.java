package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class BuildStackTraceAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getBambooToolWindowPanel(event).showBuildStackTrace();
    }

	public void update(AnActionEvent event) {
		if (IdeaHelper.getBambooToolWindowPanel(event) != null) {
			event.getPresentation().setEnabled(IdeaHelper.getBambooToolWindowPanel(event).canShowFailedTests());
		}
		super.update(event);
	}
}
