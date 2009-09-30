package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author jgorycki
 */
public class ActiveIssueCommentAction extends AbstractActiveJiraIssueAction {

	public void actionPerformed(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
		if (panel == null) {
			return;
		}
		final JiraIssueAdapter issue;
		try {
			issue = ActiveIssueUtils.getJIRAIssue(event);
		} catch (JIRAException e) {
			panel.setStatusErrorMessage("Error commenting issue: " + e.getMessage(), e);
			return;
		}
		if (IdeaHelper.getProjectCfgManager(event) != null) {
			JiraServerData jira = ActiveIssueUtils.getJiraServer(event);
			if (jira != null) {
				panel.addCommentToIssue(issue.getKey(), jira);
			}
		}
	}

	public void onUpdate(AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		event.getPresentation().setEnabled(enabled);
	}
}
