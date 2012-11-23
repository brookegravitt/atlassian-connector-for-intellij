package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.Issue;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:32:22 PM
 */
public class PriorityFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        BasicPriority priority = apiIssueObject.getPriority();
        if (priority == null) {
            return null;
        }
        return ImmutableList.of(priority.getName());
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        return ImmutableList.of(apiIssueObject.getPriority());
    }
}
