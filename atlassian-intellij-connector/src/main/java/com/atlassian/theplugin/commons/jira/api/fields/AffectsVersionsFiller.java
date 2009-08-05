package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.soap.axis.RemoteIssue;
import com.atlassian.theplugin.commons.jira.api.soap.axis.RemoteVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:53:30 PM
 */
public class AffectsVersionsFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}
		RemoteVersion[] rv = ri.getAffectsVersions();
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
