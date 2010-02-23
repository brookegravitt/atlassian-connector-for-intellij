package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import org.jetbrains.annotations.NotNull;


/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:23:28
 */
public abstract class JiraPresetFilter implements JIRAQueryFragment {
    private final ServerId serverId;
    protected final ProjectCfgManager projectCfgManager;

    protected JiraPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        this.projectCfgManager = projectCfgManager;
        this.serverId = jiraServer.getServerId();
    }

    public JiraServerData getJiraServer() {
        return (JiraServerData) projectCfgManager.getJiraServerr(serverId);
    }

    public long getId() {
        return -1;
    }

    public abstract String getSortBy();


}
