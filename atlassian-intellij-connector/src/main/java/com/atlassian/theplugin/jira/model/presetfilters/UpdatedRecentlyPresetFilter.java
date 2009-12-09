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
 * Runs query for: "updated:previous=-1w&sorter/field=updated"
 */
public class UpdatedRecentlyPresetFilter extends JiraPresetFilter {
    public UpdatedRecentlyPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public String getName() {
        return "Updated recently";
    }

    public String getQueryString() {
        return "updated:previous=-1w";
    }

    public String getSortBy() {
        return "updated";
    }
}
