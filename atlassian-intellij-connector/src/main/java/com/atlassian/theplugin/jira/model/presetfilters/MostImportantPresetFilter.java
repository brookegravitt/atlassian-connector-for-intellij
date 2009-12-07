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
 * Runs query for: "resolution=-1&sorter/field=priority"
 */
public class MostImportantPresetFilter extends JiraPresetFilter {
    public MostImportantPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager,jiraServer);
    }

    public String getName() {
        return "Most important";
    }

    public String getQueryString() {
        return "resolution=-1";
    }

    public String getSortBy() {
        return "priority";
    }
}