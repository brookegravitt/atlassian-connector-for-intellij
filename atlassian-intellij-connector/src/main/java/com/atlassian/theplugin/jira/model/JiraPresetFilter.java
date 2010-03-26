package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
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
    private JIRAProject jiraProject;

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
    
    final public String getQueryStringFragment() {
        final String s = getQuery();
        return s != null && s.length() > 0 ? s + getProjectQueryString() : "";
    }

    public abstract String getSortBy();
    protected  abstract String getQuery();

    public JIRAProject getJiraProject() {
        return jiraProject;
    }

    public void setJiraProject(JIRAProject jiraProject) {
        this.jiraProject = jiraProject;
    }

    private String getProjectQueryString() {
        if (jiraProject != null) {
            return "&pid=" + jiraProject.getId();
        }

        return "";
    }

    public String getProjectKey() {
        return jiraProject != null && jiraProject.getKey() != null ? jiraProject.getKey() : "";
    }

}
