package com.atlassian.theplugin.idea.action.jira.ManualFilter;

import com.atlassian.connector.commons.jira.beans.JiraQueryUrl;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author pmaruszak
 */
public class ViewInBrowserAction extends AnAction {
    @Override
    public void update(AnActionEvent event) {
        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
        JiraCustomFilter manualFilter = panel != null ? panel.getSelectedManualFilter() : null;

        boolean enabled = (panel != null && manualFilter != null && !manualFilter.isEmpty());
        event.getPresentation().setEnabled(enabled);

    }

    public void actionPerformed(AnActionEvent event) {
        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);

        if (panel == null) {
            return;
        }

        JiraCustomFilter manualFilter = panel.getSelectedManualFilter();
        ServerData jiraServer = panel.getSelectedServer();
        if (manualFilter != null && jiraServer != null) {
            JiraQueryUrl queryUrl = new JiraQueryUrl.Builder()
                    .serverUrl(jiraServer.getUrl())
                    .queryFragments(manualFilter.getQueryFragments())
                    .max(100)
                    .build();
            try {
                if (jiraServer.isUseBasicUser()) {
                    //if user uses basic authentication then authenticate first
                    IntelliJJiraServerFacade.getInstance().testServerConnection(jiraServer);
                }
            } catch (RemoteApiException e) {
                //nothing special
            }
            BrowserUtil.launchBrowser(queryUrl.buildIssueNavigatorUrl());
        }
    }
}
