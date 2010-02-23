package com.atlassian.theplugin.jira.model.presetfilters;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:42:34
 *
 * Runs query for: "sorter/field=priority"
 */
public class AllPresetFilter extends JiraPresetFilter {
    public AllPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }


    public String getQueryStringFragment() {
        return "";
    }

    public long getId() {
        return 0;
    }

    public String getName() {
        return "All";
    }

    public HashMap<String, String> getMap() {
        return new HashMap<String, String>();
    }

    public JIRAQueryFragment getClone() {
        return new AllPresetFilter(super.projectCfgManager, getJiraServer());
    }

    public String getSortBy() {
        return "priority";
    }
}