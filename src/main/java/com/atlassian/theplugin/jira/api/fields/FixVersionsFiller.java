package com.atlassian.theplugin.jira.api.fields;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue;
import com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:53:43 PM
 */
public class FixVersionsFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}
		RemoteVersion[] rv = ri.getFixVersions();
		List<String> result = new ArrayList<String>();
		if (rv == null) {
			return null;
		}
		for (RemoteVersion version : rv) {
			result.add(version.getId());
		}
		return result;
	}
}
