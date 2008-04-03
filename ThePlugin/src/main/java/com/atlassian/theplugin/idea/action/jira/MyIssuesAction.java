package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;

public class MyIssuesAction extends CheckboxAction implements JIRAQueryFragment {
    private boolean state = true;
    public static final String QF_NAME = "myissues";

    public boolean isSelected(AnActionEvent event) {
        return state;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSelected(AnActionEvent event, boolean b) {
        state = b;

        JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(event);
        if (b) {
            toolWindowPanel.addQueryFragment(QF_NAME, this);
        } else {
            toolWindowPanel.addQueryFragment(QF_NAME, null);
        }
        toolWindowPanel.refreshIssues();
    }

    public String getQueryStringFragment() {
        return "assignee=" + IdeaHelper.getCurrentJIRAServer().getServer().getUserName();
    }

	public String getName() {
		return "";
	}
}
