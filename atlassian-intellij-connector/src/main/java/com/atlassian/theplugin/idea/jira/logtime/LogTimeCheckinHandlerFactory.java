/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.theplugin.idea.jira.logtime;

import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.jira.LogTimeCheckinHandler;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import org.jetbrains.annotations.NotNull;

public class LogTimeCheckinHandlerFactory extends CheckinHandlerFactory {
    private LogTimeCheckinHandler handler;
     //
    public LogTimeCheckinHandlerFactory(JiraWorkspaceConfiguration jiraWorkspaceConfiguration) {

        handler = new LogTimeCheckinHandler(jiraWorkspaceConfiguration);
    }

    public CheckinHandler createHandler(CheckinProjectPanel checkinprojectpanel) {
        return handler.createHandler(checkinprojectpanel);
    }

    @NotNull
    @Override
    public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel, CommitContext commitContext) {
        return handler.createHandler(checkinProjectPanel);
    }
}
