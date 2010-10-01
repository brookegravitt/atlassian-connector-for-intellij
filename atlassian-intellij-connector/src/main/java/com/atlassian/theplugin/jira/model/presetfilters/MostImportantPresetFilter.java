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
 * Runs query for: "resolution=-1&sorter/field=priority"
 */
public class MostImportantPresetFilter extends JiraPresetFilter {
    public MostImportantPresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public String getName() {
        return "Most important";
    }

    public HashMap<String, String> getMap() {
        HashMap<String,  String> map = new  HashMap<String,  String>();
        map.put("resolution", "-1");
        return map;
    }

    public JIRAQueryFragment getClone() {
        return new MostImportantPresetFilter(projectCfgManager, getJiraServer());
    }


    public String getSortBy() {
        return "priority";
    }

    @Override
    protected String getQuery() {
        return "resolution=-1";
    }
}