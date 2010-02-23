package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public abstract class JIRAAbstractAction extends AnAction {
	public abstract void onUpdate(AnActionEvent event);

	public void onUpdate(AnActionEvent event, boolean enabled) {
	}


	@Override
	public final void update(AnActionEvent event) {
		super.update(event);
//
//		boolean enabled = ModelFreezeUpdater.getState(event);
//
//		if (enabled) {
//			onUpdate(event);
//		}
//		onUpdate(event, enabled);

		boolean enabled = false;
		JiraServerData server = null;
		JiraIssueAdapter selectedIssue = event.getData(Constants.ISSUE_KEY);
		if (selectedIssue != null) {
            server = selectedIssue.getJiraServerData();
		}
		if (server == null && event.getData(Constants.SERVER_KEY) != null) {
			server = (JiraServerData) event.getData(Constants.SERVER_KEY);
		}

        IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);

		enabled = server != null && server.isEnabled() && panel.isRefreshing();

		event.getPresentation().setEnabled(enabled);

		if (enabled) {
			onUpdate(event);
		}
		onUpdate(event, enabled);
	}

    private JiraServerData getJiraServerData(AnActionEvent event, String serverId) {
      ProjectCfgManager projectCfgManager = IdeaHelper.getProjectCfgManager(event);
      return (JiraServerData) projectCfgManager.getJiraServerr(new ServerIdImpl(serverId));
    }
}
