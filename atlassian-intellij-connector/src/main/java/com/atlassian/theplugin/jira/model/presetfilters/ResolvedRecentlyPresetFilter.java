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
 * Runs query for: "status=5&status=6&updated:previous=-1w&sorter/field=updated"
 */
public class ResolvedRecentlyPresetFilter extends JiraPresetFilter {
    public ResolvedRecentlyPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public String getQueryStringFragment() {
        return  "status=5&status=6&updated:previous=-1w";
    }

    public String getName() {
        return "Resolved recently";
    }

    public HashMap<String, String> getMap() {
        HashMap<String,  String> map = new  HashMap<String,  String>();
        map.put("status", "5");
        map.put("status", "6");
        map.put("updated:previous", "-1w");
        return map;
    }

    public JIRAQueryFragment getClone() {
        return new ResolvedRecentlyPresetFilter(projectCfgManager, getJiraServer());
    }


    public String getSortBy() {
        return "updated";
    }
}