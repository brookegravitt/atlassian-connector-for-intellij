package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 3:59:25 PM
 */
public interface FieldFiller {
	List<String> getFieldValues(String field, JIRAIssue detailedIssue);
}
