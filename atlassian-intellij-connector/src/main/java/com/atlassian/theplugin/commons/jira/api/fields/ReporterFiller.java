package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:28:52 PM
 */
public class ReporterFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        BasicUser reporter = apiIssueObject.getReporter();
        if (reporter == null) {
            return null;
        }
        return ImmutableList.of(reporter.getName());
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        return ImmutableList.of(apiIssueObject.getReporter());
    }
}
