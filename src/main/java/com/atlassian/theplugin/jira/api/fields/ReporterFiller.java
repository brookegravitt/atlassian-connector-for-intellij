package com.atlassian.theplugin.jira.api.fields;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:28:52 PM
 */
public class ReporterFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		List<String> result = new ArrayList<String>();

		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}


		result.add(ri.getReporter());
		return result;
	}
}
