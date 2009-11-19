package com.atlassian.theplugin.jira.model.presetfilters;

import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import com.atlassian.theplugin.commons.jira.JiraServerData;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:42:34
 *
 * Runs query for: "updated:previous=-1w&sorter/field=updated"
 */
public class UpdatedRecentlyPresetFilter extends JiraPresetFilter {
    public UpdatedRecentlyPresetFilter(JiraServerData jiraServer) {
        super(jiraServer);
    }

    public String getName() {
        return "Updated recently";
    }

    public String getQueryString() {
        return "updated:previous=-1w";
    }

    public String getSortBy() {
        return "updated";
    }
}
