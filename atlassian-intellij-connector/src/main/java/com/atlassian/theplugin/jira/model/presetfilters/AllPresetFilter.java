package com.atlassian.theplugin.jira.model.presetfilters;

import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import com.atlassian.theplugin.commons.jira.JiraServerData;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:42:34
 *
 * Runs query for: "sorter/field=priority"
 */
public class AllPresetFilter extends JiraPresetFilter {
    public AllPresetFilter(JiraServerData jiraServer) {
        super(jiraServer);
    }

    public String getName() {
        return "All";
    }

    public String getQueryString() {
        return "";
    }

    public String getSortBy() {
        return "priority";
    }
}