package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: kalamon
 * Date: 23.11.12
 * Time: 11:51
 */
public abstract class AbstractFieldFiller implements FieldFiller {
    @Override
    public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
        Object apiIssueObject = detailedIssue.getApiIssueObject();
        if (apiIssueObject instanceof RemoteIssue) {
            return getFieldValues(field, detailedIssue, ((RemoteIssue) apiIssueObject));
        } else if (apiIssueObject instanceof Issue) {
            return getFieldValues(field, detailedIssue, ((Issue) apiIssueObject));
        }
        return Lists.newArrayList();
    }

    protected abstract List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject);

    protected abstract List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject);
}
