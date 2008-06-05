package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class CrucibleAddCustomFilterAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        IdeaHelper.getCrucibleToolWindowPanel(event).addCustomFilter();
    }
}
