package com.atlassian.theplugin.jira.api.fields;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:31:16 PM
 */
public class DefaultEmptyFieldFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		List<String> result = new ArrayList<String>();
		result.add("");
		System.out.println("DefaultEmptyFieldFiller called! booo! This call must not be used in production! beware!");
		return result;
	}
}
