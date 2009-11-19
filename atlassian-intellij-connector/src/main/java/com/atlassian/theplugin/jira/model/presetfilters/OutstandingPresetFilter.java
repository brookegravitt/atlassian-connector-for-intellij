package com.atlassian.theplugin.jira.model.presetfilters;

import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import com.atlassian.theplugin.commons.jira.JiraServerData;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:42:34
 *
 * Runs query for: "resolution=-1&sorter/field=updated"
 */
public class OutstandingPresetFilter extends JiraPresetFilter {
    public OutstandingPresetFilter(JiraServerData jiraServer) {
        super(jiraServer);
    }

    public String getName() {
        return "Outstanding";
    }

    public String getQueryString() {
        return "resolution=-1";
    }

    public String getSortBy() {
        return "updated";
    }
}