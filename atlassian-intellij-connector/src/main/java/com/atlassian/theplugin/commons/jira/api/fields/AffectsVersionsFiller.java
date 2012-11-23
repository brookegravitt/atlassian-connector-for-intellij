package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteVersion;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Version;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:53:30 PM
 */
public class AffectsVersionsFiller  extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        Iterable<Version> affectedVersions = apiIssueObject.getAffectedVersions();
        if (affectedVersions == null) {
            return null;
        }
        List<String> result = Lists.newArrayList();
        for (Version version : affectedVersions) {
            Long id = version.getId();
            if (id == null) {
                continue;
            }
            result.add(id.toString());
        }
        return result;
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        RemoteVersion[] rv = apiIssueObject.getAffectsVersions();
        List<String> result = Lists.newArrayList();
        if (rv == null) {
            return null;
        }
        for (RemoteVersion version : rv) {
            result.add(version.getId());
        }
        return result;
    }
}
