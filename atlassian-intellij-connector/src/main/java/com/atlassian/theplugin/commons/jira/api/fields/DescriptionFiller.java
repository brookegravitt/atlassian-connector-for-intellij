package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:36:50 PM
 */
public class DescriptionFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
        if (detailedIssue.getApiIssueObject() instanceof RemoteIssue) {
            RemoteIssue ri = (RemoteIssue) detailedIssue.getApiIssueObject();
            if (ri == null) {
                return null;
            }
            return ImmutableList.of(Optional.fromNullable(ri.getDescription()).or(""));
        }
        return ImmutableList.of(Optional.fromNullable(detailedIssue.getWikiDescription()).or(""));
	}
}
