package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class LabelBuildAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getBambooToolWindowPanel(event).addLabelToBuild();
    }

	public void update(AnActionEvent event) {
		if (IdeaHelper.getBambooToolWindowPanel(event) != null) {
			boolean enabled = IdeaHelper.getBambooToolWindowPanel(event).getLabelBuildEnabled();
			event.getPresentation().setEnabled(enabled);
		}
		super.update(event);
	}
}