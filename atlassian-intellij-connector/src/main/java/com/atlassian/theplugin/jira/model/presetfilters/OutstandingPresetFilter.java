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
 * Runs query for: "resolution=-1&sorter/field=updated"
 */
public class OutstandingPresetFilter extends JiraPresetFilter {
    public OutstandingPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public String getQueryStringFragment() {
        return "resolution=-1";
    }

    public String getName() {
        return "Outstanding";
    }

    public HashMap<String, String> getMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("assigneeSelect", "issue_current_user");
        map.put("resolution", "-1");
        return map;
    }

    public JIRAQueryFragment getClone() {
        return new OutstandingPresetFilter(projectCfgManager, getJiraServer());
    }


    public String getSortBy() {
        return "updated";
    }
}