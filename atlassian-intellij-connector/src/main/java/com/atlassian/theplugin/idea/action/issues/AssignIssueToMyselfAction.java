package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AssignIssueToMyselfAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
	    IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(anActionEvent);
		if (panel != null) {
			panel.assignIssueToMyself();
		}
	}

	public void onUpdate(AnActionEvent event) {		
	}
}
