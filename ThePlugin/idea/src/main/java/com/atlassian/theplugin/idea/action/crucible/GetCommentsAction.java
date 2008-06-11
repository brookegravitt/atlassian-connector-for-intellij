package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;


public class GetCommentsAction extends AnAction {
    public void actionPerformed(AnActionEvent anActionEvent) {
        IdeaHelper.getCrucibleToolWindowPanel(anActionEvent).getReviewComments();
    }
}
