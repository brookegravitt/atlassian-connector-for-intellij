package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.commons.cfg.AbstractCfgManager;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Mar 26, 2009
 * Time: 3:51:19 PM
 */
public class ActiveIssueCommentAction extends AbstractActiveJiraIssueAction {

	public void actionPerformed(AnActionEvent event) {
		JIRAIssue issue = null;
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		try {
			issue = ActiveIssueUtils.getJIRAIssue(event);
		} catch (JIRAException e) {
			if (panel != null) {
				panel.setStatusMessage("Error commenting issue: " + e.getMessage(), true);
			}
		}
		ServerData serverData = null;
		if (IdeaHelper.getCfgManager(event) != null) {
			serverData = IdeaHelper.getCfgManager(event).getServerData(ActiveIssueUtils.getJiraServer(event));
		}
		if (issue != null && panel != null) {

			panel.addCommentToIssue(issue.getKey(),
					serverData);
		}
	}

	public void onUpdate(AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		event.getPresentation().setEnabled(enabled);
	}
}
