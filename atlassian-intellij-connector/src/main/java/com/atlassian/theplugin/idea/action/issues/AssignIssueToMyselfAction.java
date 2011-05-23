package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AssignIssueToMyselfAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent anActionEvent) {
		final JiraIssueAdapter issue = anActionEvent.getData(Constants.ISSUE_KEY);
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(anActionEvent);
		if (issue != null && panel != null) {
			panel.assignIssueToMyself(issue);
		}
	}

	public void onUpdate(AnActionEvent event) {
		final JiraIssueAdapter issue = event.getData(Constants.ISSUE_KEY);
		ServerData server = event.getData(Constants.SERVER_KEY);

		if (server != null && issue != null) {
			if (issue.getAssigneeId() != null && issue.getAssigneeId().equals(server.getUsername())) {
				event.getPresentation().setEnabled(false);
			}
		}
	}
}
