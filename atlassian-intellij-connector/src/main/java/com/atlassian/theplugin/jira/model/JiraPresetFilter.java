package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.JiraServerData;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:23:28
 */
public abstract class JiraPresetFilter {
    private final JiraServerData jiraServer;

    protected JiraPresetFilter(JiraServerData jiraServer) {
        this.jiraServer = jiraServer;
    }

    public JiraServerData getJiraServer() {
        return jiraServer;
    }

    public abstract String getName();
    public abstract String getQueryString();

    public abstract String getSortBy();
}
