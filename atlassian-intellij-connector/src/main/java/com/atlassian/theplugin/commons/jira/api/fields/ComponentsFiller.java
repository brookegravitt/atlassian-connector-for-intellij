package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteComponent;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:53:06 PM
 */
public class ComponentsFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}
		RemoteComponent[] components = ri.getComponents();
		List<String> result = new ArrayList<String>();
		if (components == null) {
			return null;
		}
		for (RemoteComponent component : components) {
			result.add(component.getId());
		}
		return result;
	}
}
