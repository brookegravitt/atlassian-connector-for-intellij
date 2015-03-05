package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:33:08 PM
 */
public class IssueTypeFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        return ImmutableList.of(apiIssueObject.getIssueType().getName());
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        return ImmutableList.of(apiIssueObject.getType());
    }

    @Override
    public FieldInput generateJrJcFieldValue(JIRAIssue issue, JIRAActionField field, JSONObject fieldMetadata) throws JSONException {
        List<String> values = field.getValues();
        if (values == null || values.size() == 0) {
            return new FieldInput(field.getFieldId(), null);
        }
        return new FieldInput(field.getFieldId(), new ComplexIssueInputFieldValue(ImmutableMap.of("id", (Object) values.get(0))));
    }
}
