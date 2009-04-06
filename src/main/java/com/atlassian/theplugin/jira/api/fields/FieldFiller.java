package com.atlassian.theplugin.jira.api.fields;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 3:59:25 PM
 */
public interface FieldFiller {
	List<String> getFieldValues(String field, JIRAIssue detailedIssue);
}
