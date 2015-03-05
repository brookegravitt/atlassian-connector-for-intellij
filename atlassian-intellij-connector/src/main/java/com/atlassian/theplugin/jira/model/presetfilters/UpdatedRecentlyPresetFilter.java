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
 * Runs query for: "updated:previous=-1w&sorter/field=updated"
 */
public class UpdatedRecentlyPresetFilter extends JiraPresetFilter {
    public UpdatedRecentlyPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public String getName() {
        return "Updated recently";
    }

    public HashMap<String, String> getMap() {
           HashMap<String, String> map = new HashMap<String, String>();
		map.put("updated:previous", "-1w");
		return map;
    }

    public JIRAQueryFragment getClone() {
        return new UpdatedRecentlyPresetFilter(projectCfgManager, getJiraServer());
    }

    public String getSortBy() {
        return "updated";
    }

    @Override
    protected String getQuery() {
         return "updated:previous=-1w";
    }

    @Override
    public String getJqlNoProject() {
        return "updated < -1w";
    }
}
