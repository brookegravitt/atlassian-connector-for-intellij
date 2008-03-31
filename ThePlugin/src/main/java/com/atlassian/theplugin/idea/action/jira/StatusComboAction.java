package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.jira.JIRAServer;

import java.util.List;

public class StatusComboAction extends AbstractConstantComboAction {
    protected List getValues(JIRAServer server) {
        return server.getStatuses();
    }

    protected String getDefaultText() {
        return "  Any Status  ";
    }
}