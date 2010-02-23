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
 * Runs query for: "assigneeSelect=issue_current_user&resolution=-1&sorter/field=priority"
 */
public class AssignedToMePresetFilter extends JiraPresetFilter {
    public AssignedToMePresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public String getQueryStringFragment() {
        return "assigneeSelect=issue_current_user&resolution=-1";
    }

    public String getName() {
        return "Assigned to me";
    }

    public HashMap<String, String> getMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("assigneeSelect", "issue_current_user");
        map.put("resolution", "-1");
        return map;
    }

    public JIRAQueryFragment getClone() {
        return new AssignedToMePresetFilter(projectCfgManager, getJiraServer());
    }


    public String getSortBy() {
        return "priority";
    }
}