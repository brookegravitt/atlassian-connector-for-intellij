package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JiraFilter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.util.JQLUtil;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:23:28
 */
public abstract class JiraPresetFilter implements JiraFilter {
    private final ServerId serverId;
    protected final ProjectCfgManager projectCfgManager;
    private JIRAProject jiraProject;
    protected abstract String getJqlNoProject();

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
    
    public final String getQueryStringFragment() {
        final String s = getQuery();
        return s != null && s.length() > 0 ? s + getProjectQueryString() : "";
    }

    @Override
    public List<JIRAQueryFragment> getQueryFragments() {
        return ImmutableList.of((JIRAQueryFragment) this);
    }

    @Override
    public String getOldStyleQueryString() {
        return getQueryStringFragment();
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

    @Override
    public String getJql() {
        String query = getJqlNoProject();
        if (jiraProject == null) {
            return query;
        }
        return query + " and project = " + JQLUtil.escapeReservedJQLKeyword(jiraProject.getKey());
    }

    public String getProjectKey() {
        return jiraProject != null && jiraProject.getKey() != null ? jiraProject.getKey() : "";
    }

}
