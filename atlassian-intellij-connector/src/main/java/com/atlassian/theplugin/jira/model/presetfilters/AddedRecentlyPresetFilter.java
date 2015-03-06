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
 * Runs query for: "created:previous=-1w&sorter/field=created"
 */
public class AddedRecentlyPresetFilter extends JiraPresetFilter {
    public AddedRecentlyPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);

    }

    public long getId() {
        return 0;
    }

    public String getName() {
        return "Added recently";
    }

    public HashMap<String, String> getMap() {
        HashMap<String, String> map = new HashMap<String, String>();
		map.put("created:previous", "-1w");
		return map;
    }

    public JIRAQueryFragment getClone() {
        return new AddedRecentlyPresetFilter(projectCfgManager, getJiraServer());
    }



    public String getSortBy() {
        return "created";
    }

    @Override
    protected String getQuery() {
         return "created:previous=-1w";
    }

    @Override
    public String getJqlNoProject() {
        return "created < -1w";
    }
}