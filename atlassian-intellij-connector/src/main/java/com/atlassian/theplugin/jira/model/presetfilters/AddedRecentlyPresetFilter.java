package com.atlassian.theplugin.jira.model.presetfilters;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import org.jetbrains.annotations.NotNull;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:42:34
 *
 * Runs query for: "created:previous=-1w&sorter/field=created"
 */
public class AddedRecentlyPresetFilter extends JiraPresetFilter {
    public AddedRecentlyPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager,jiraServer);
    }

    public String getName() {
        return "Added recently";
    }

    public String getQueryString() {
        return "created:previous=-1w";
    }

    public String getSortBy() {
        return "created";
    }
}