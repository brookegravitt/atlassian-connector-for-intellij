package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;

public class FilterTypeAction extends CheckboxAction {
    private boolean state = true;

    public boolean isSelected(AnActionEvent event) {
        return state; 
    }

    public void setSelected(AnActionEvent event, boolean b) {
        state = b;

        JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(event);
        if (b) {
            toolWindowPanel.getFilters().setSavedFilterUsed(true);
        } else {
            toolWindowPanel.getFilters().setSavedFilterUsed(false);
        }
        toolWindowPanel.refreshIssues();
    }
}
