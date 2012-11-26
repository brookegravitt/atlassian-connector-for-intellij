package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.BasicUser;
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
 * Time: 4:27:03 PM
 */
public class AssigneeFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        BasicUser assignee = apiIssueObject.getAssignee();
        if (assignee == null) {
            return null;
        }
        return ImmutableList.of(assignee.getName());
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        return ImmutableList.of(apiIssueObject.getAssignee());
    }

    @Override
    public FieldInput generateJrJcFieldValue(JIRAIssue issue, JIRAActionField field, JSONObject fieldMetadata) throws JSONException {
        List<String> values = field.getValues();
        if (values == null || values.size() == 0) {
            return new FieldInput(field.getFieldId(), null);
        }
        return new FieldInput(field.getFieldId(), new ComplexIssueInputFieldValue(ImmutableMap.of("name", (Object) values.get(0))));
    }
}
