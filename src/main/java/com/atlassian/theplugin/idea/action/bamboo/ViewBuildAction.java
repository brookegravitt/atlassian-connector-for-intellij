package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class ViewBuildAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        IdeaHelper.getBambooToolWindowPanel(event).viewBuild();
    }
}
