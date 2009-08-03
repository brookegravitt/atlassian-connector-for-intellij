package com.atlassian.theplugin.idea.action.ManualFilter;

import com.atlassian.theplugin.commons.jira.api.JiraQueryUrl;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;
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
        JIRAManualFilter manualFilter = panel != null ? panel.getSelectedManualFilter() : null;

        boolean enabled =  (panel != null && manualFilter != null && !manualFilter.isEmpty());
        event.getPresentation().setEnabled(enabled);

    }

    public void actionPerformed(AnActionEvent event) {
          final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);

        if (panel == null) {
            return;
        }

        JIRAManualFilter manualFilter = panel.getSelectedManualFilter();
        ServerData jiraServer = panel.getSelectedServer();
        if (manualFilter != null && jiraServer != null) {
            JiraQueryUrl queryUrl = new JiraQueryUrl.Builder()
                    .serverUrl(jiraServer.getUrl())
                    .queryFragments(manualFilter.getQueryFragment())                    
                    .max(100)
                    .build();
            BrowserUtil.launchBrowser(queryUrl.buildIssueNavigatorUrl());
        }
    }
}
