package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;

public class UnresolvedIssuesAction extends CheckboxAction implements JIRAQueryFragment {
    private boolean state = true;
    public static final String QF_NAME = "unresolved";

    public boolean isSelected(AnActionEvent event) {
        return state;
    }

    public void setSelected(AnActionEvent event, boolean b) {
        state = b;

        JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(event);
        if (b) {
            toolWindowPanel.addQueryFragment(QF_NAME, this);
        } else {
            IdeaHelper.getJIRAToolWindowPanel(event).addQueryFragment(QF_NAME, null);
        }
        toolWindowPanel.refreshIssues();
    }

    public String getQueryStringFragment() {
        return "resolution=-1";
    }
}