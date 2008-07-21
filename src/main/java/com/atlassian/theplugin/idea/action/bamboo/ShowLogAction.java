package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class ShowLogAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        IdeaHelper.getBambooToolWindowPanel(event).showBuildLog();        
    }

	public void update(AnActionEvent event) {
		if (IdeaHelper.getBambooToolWindowPanel(event) != null) {
			boolean enabled = IdeaHelper.getBambooToolWindowPanel(event).getLabelBuildEnabled();
			event.getPresentation().setEnabled(enabled);
		}
		super.update(event);
	}    
}
