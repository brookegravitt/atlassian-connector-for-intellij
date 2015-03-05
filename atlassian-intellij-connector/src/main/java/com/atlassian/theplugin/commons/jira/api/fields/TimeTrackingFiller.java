package com.atlassian.theplugin.commons.jira.api.fields;


import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.google.common.base.Optional;
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

    private int compareEstimated(String estimate1, String estimate2) {
        return translate(estimate1).compareTo(translate(estimate2));
    }

    @Override
    public FieldInput generateJrJcFieldValue(JIRAIssue issue, JIRAActionField field, JSONObject fieldMetadata) throws JSONException, RemoteApiException {
        Object value = null;
        List<String> values = field.getValues();
        if (values != null && values.size() > 0) {
            if (issue.getTimeSpent() == null) {
                // unnecessarily updating originalEstimate can result in an exception when JIRA time tracking is in legacy mode
                if (isOriginalEstimateChanged(issue, values) || isFieldRequired(fieldMetadata)) {
                    value = new ComplexIssueInputFieldValue(ImmutableMap.of("originalEstimate", (Object) translate(values.get(0))));
                }
            } else {
                value = new ComplexIssueInputFieldValue(ImmutableMap.of("remainingEstimate", (Object) translate(values.get(0))));
            }
        }
        return value != null ? new FieldInput(field.getFieldId(), value) : null;
    }

    private boolean isFieldRequired(JSONObject fieldMetadata) throws JSONException {
        return (fieldMetadata.has("required") && ((Boolean)fieldMetadata.get("required")) == true);
    }

    private boolean isOriginalEstimateChanged(JIRAIssue issue, List<String> values) {
        return (compareEstimated(values.get(0), Optional.fromNullable(issue.getOriginalEstimate()).or("0m")) != 0);
    }
}
