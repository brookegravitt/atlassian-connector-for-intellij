package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;


public class AssignIssueAndStartWorkAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent e) {
		final JIRAIssue issue = e.getData(Constants.ISSUE_KEY);
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
		if (issue != null && panel != null) {
			panel.startWorkingOnIssue(issue);
		}
	}

	public void onUpdate(AnActionEvent event) {
		final JIRAIssue issue = event.getData(Constants.ISSUE_KEY);
		ServerCfg server = event.getData(Constants.SERVER_KEY);

		if (server != null && issue != null) {
			if (issue.getAssigneeId().equals(server.getUsername())) {
				event.getPresentation().setText("Start Working");
			} else {
				event.getPresentation().setText("Assign to Me and Start Working");
			}
		}
	}
}
