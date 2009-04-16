package com.atlassian.theplugin.jira.api.fields;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.Arrays;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:30:12 PM
 */
public class TimeTrackingFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {

		if (detailedIssue.getTimeSpent() == null) {
			return Arrays.asList(translate(detailedIssue.getOriginalEstimate()));
		}

		return Arrays.asList(translate(detailedIssue.getRemainingEstimate()));
	}

	private String translate(String displayValue) {
		if (displayValue != null) {
			displayValue = displayValue.replaceAll(" week", "w");
			displayValue = displayValue.replaceAll(" day", "d");
			displayValue = displayValue.replaceAll(" hour", "h");
			displayValue = displayValue.replaceAll(" minute", "m");
			displayValue = displayValue.replaceAll(",", "");
		}
		return displayValue;
	}
}
