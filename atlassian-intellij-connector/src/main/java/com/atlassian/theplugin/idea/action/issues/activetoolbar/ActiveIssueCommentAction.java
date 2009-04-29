package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author jgorycki
 */
public class ActiveIssueCommentAction extends AbstractActiveJiraIssueAction {

	public void actionPerformed(AnActionEvent event) {
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel == null) {
			return;
		}
		final JIRAIssue issue;
		try {
			issue = ActiveIssueUtils.getJIRAIssue(event);
		} catch (JIRAException e) {
			panel.setStatusMessage("Error commenting issue: " + e.getMessage(), true);
			return;
		}
		if (IdeaHelper.getCfgManager(event) != null) {
			JiraServerCfg jira = ActiveIssueUtils.getJiraServer(event);
			if (jira != null) {
				ServerData serverData = IdeaHelper.getProjectCfgManager(event).getServerData(jira);
				panel.addCommentToIssue(issue.getKey(), serverData);
			}
		}
	}

	public void onUpdate(AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		event.getPresentation().setEnabled(enabled);
	}
}
