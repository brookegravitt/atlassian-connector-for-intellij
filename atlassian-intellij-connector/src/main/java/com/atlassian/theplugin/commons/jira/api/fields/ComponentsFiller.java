package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteComponent;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.Issue;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:53:06 PM
 */
public class ComponentsFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        Iterable<BasicComponent> components = apiIssueObject.getComponents();
        if (components == null) {
            return null;
        }
        List<String> result = Lists.newArrayList();
        for (BasicComponent component : components) {
            Long id = component.getId();
            if (id == null) {
                continue;
            }
            result.add(id.toString());
        }
        return result;
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        RemoteComponent[] components = apiIssueObject.getComponents();
        List<String> result = Lists.newArrayList();
        if (components == null) {
            return null;
        }
        for (RemoteComponent c : components) {
            result.add(c.getId());
        }
        return result;
    }
}
