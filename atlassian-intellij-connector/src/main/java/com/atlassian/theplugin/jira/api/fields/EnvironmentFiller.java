package com.atlassian.theplugin.jira.api.fields;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:33:46 PM
 */
public class EnvironmentFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		result.add(ri.getEnvironment());
		return result;
	}
}
