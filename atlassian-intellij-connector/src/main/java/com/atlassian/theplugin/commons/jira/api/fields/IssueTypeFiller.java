package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.commons.soap.axis.RemoteIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:33:08 PM
 */
public class IssueTypeFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		result.add(ri.getType());
		return result;
	}
}
