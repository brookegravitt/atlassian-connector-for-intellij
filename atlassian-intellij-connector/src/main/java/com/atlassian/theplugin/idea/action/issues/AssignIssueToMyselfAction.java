package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AssignIssueToMyselfAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
		final JIRAIssue issue = anActionEvent.getData(Constants.ISSUE_KEY);
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(anActionEvent);
		if (issue != null && panel != null) {
			panel.assignIssueToMyself(issue);
		}
	}

	public void onUpdate(AnActionEvent event) {
		final JIRAIssue issue = event.getData(Constants.ISSUE_KEY);
		ServerData server = event.getData(Constants.SERVER_KEY);

		if (server != null && issue != null) {
			if (issue.getAssigneeId().equals(server.getUserName())) {
				event.getPresentation().setEnabled(false);
			}
		}
	}
}
