package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:36:50 PM
 */
public class DescriptionFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        return ImmutableList.of(Optional.fromNullable(apiIssueObject.getDescription()).or(""));
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        return ImmutableList.of(Optional.fromNullable(apiIssueObject.getDescription()).or(""));
    }

    @Override
    public FieldInput generateJrJcFieldValue(JIRAIssue issue, JIRAActionField field, JSONObject fieldMetadata) throws JSONException {
        List<String> values = field.getValues();
        return new FieldInput(field.getFieldId(), values != null && values.size() > 0 ? values.get(0) : null);
    }
}
