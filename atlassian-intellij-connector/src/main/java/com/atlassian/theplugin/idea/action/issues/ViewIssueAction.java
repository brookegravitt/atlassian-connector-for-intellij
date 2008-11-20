package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ViewIssueAction extends JIRAAbstractAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (panel != null) {
			panel.viewIssueInBrowser();
		}
	}


	public void onUpdate(AnActionEvent event) {
	}

	public void onUpdate(AnActionEvent event, boolean enabled) {
		if (enabled){
			event.getPresentation().setEnabled(event.getData(Constants.ISSUE_KEY) != null);
		}
	}
}
