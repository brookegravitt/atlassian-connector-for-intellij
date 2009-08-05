package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.soap.axis.RemoteCustomFieldValue;
import com.atlassian.theplugin.commons.jira.api.soap.axis.RemoteIssue;

import java.util.Arrays;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:01:14 PM
 */
public class CustomFieldFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}
		RemoteCustomFieldValue[] customFields = ri.getCustomFieldValues();
		for (RemoteCustomFieldValue customField : customFields) {
			if (customField.getCustomfieldId().equals(field)) {
				return Arrays.asList(customField.getValues());
			}
		}
		return null;
	}
}
