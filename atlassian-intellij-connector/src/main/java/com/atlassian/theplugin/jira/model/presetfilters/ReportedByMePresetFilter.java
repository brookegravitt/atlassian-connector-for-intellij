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
 * Runs query for: "reporterSelect=issue_current_user&sorter/field=updated"
 *
 * note that on teh web this preset sorts by priority. I have changed it because it seems to make a bit more sense
 *
 */
public class ReportedByMePresetFilter extends JiraPresetFilter {
    public ReportedByMePresetFilter(@NotNull ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public String getName() {
        return "Reported by me";
    }

    public HashMap<String, String> getMap() {
        HashMap<String,  String> map = new  HashMap<String,  String>();
        map.put("reporterSelect", "issue_current_user");
        return map;
    }

    public JIRAQueryFragment getClone() {
        return new ReportedByMePresetFilter(projectCfgManager, getJiraServer());
    }

    public String getSortBy() {
        return "updated";
    }

    @Override
    protected String getQuery() {
        return "reporterSelect=issue_current_user";
    }

    @Override
    public String getJqlNoProject() {
        return "reporter = currentUser()";
    }
}