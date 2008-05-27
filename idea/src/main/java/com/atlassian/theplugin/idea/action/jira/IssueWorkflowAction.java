package com.atlassian.theplugin.idea.action.jira;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

public class IssueWorkflowAction extends AnAction {

    public void actionPerformed(AnActionEvent anActionEvent) {
        IdeaHelper.getJIRAToolWindowPanel(anActionEvent).showIssueActions();        
    }

}
