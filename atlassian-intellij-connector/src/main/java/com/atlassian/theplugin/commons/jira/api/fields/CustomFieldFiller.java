package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.JiraCustomFieldImpl;
import com.atlassian.connector.commons.jira.soap.axis.RemoteCustomFieldValue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:01:14 PM
 */
public class CustomFieldFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        Field fld = apiIssueObject.getField(field);
        if (fld == null) {
            return null;
        }
//        List<Field> customFields = Lists.newArrayList();
//        for (Field f : apiIssueObject.getFields()) {
//            if (field.equals(f.getId().startsWith("customfield_")) {
//                customFields.add(f);
//            }
//        }

        if (JiraCustomFieldImpl.BasicKeyType.getValueOf(fld.getType()) == JiraCustomFieldImpl.BasicKeyType.UNSUPPORTED) {
            return null;
        }
        // is it ok?
        return ImmutableList.of(fld.getValue().toString());
//        List<String> result = Lists.newArrayList();
//        for (Field cf : customFields) {
//            if (JiraCustomFieldImpl.BasicKeyType.getValueOf(cf.getType()) == JiraCustomFieldImpl.BasicKeyType.UNSUPPORTED) {
//                continue;
//            }
//            result.add(cf.getValue().toString());
//        }
//        return result;
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        RemoteCustomFieldValue[] customFields = apiIssueObject.getCustomFieldValues();
        for (RemoteCustomFieldValue customField : customFields) {
            if (customField.getCustomfieldId().equals(field)) {
                return Arrays.asList(customField.getValues());
            }
        }
        return null;
    }
}
