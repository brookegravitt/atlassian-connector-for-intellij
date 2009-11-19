package com.atlassian.theplugin.jira.model.presetfilters;

import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import com.atlassian.theplugin.commons.jira.JiraServerData;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:42:34
 *
 * Runs query for: "status=5&status=6&updated:previous=-1w&sorter/field=updated"
 */
public class ResolvedRecentlyPresetFilter extends JiraPresetFilter {
    public ResolvedRecentlyPresetFilter(JiraServerData jiraServer) {
        super(jiraServer);
    }

    public String getName() {
        return "Resolved recently";
    }

    public String getQueryString() {
        return "status=5&status=6&updated:previous=-1w";
    }

    public String getSortBy() {
        return "updated";
    }
}