package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.jira.JIRAServer;

import java.util.List;

public class IssueTypeComboAction extends AbstractConstantComboAction {
    protected List getValues(JIRAServer server) {
        return server.getIssueTypes();
    }

    protected String getDefaultText() {
        return "Any Issue Type";
    }
}