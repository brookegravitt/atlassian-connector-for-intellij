package com.atlassian.theplugin.commons.jira.api.fields;


import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
			displayValue = displayValue.replaceAll(" weeks", "w");
			displayValue = displayValue.replaceAll(" week", "w");
			displayValue = displayValue.replaceAll(" days", "d");
			displayValue = displayValue.replaceAll(" day", "d");
			displayValue = displayValue.replaceAll(" hours", "h");
			displayValue = displayValue.replaceAll(" hour", "h");
			displayValue = displayValue.replaceAll(" minutes", "m");
			displayValue = displayValue.replaceAll(" minute", "m");
			displayValue = displayValue.replaceAll(",", "");
		}
		return displayValue;
	}

    @Override
    public FieldInput generateJrJcFieldValue(JIRAIssue issue, JIRAActionField field, JSONObject fieldMetadata) throws JSONException, RemoteApiException {
        Object value = null;
        List<String> values = field.getValues();
        if (values != null && values.size() > 0) {
            if (issue.getTimeSpent() == null) {
                value = new ComplexIssueInputFieldValue(ImmutableMap.of("originalEstimate", (Object) translate(values.get(0))));
            } else {
                value = new ComplexIssueInputFieldValue(ImmutableMap.of("remainingEstimate", (Object) translate(values.get(0))));
            }
        }
        return new FieldInput(field.getFieldId(), value);
    }
}
