package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.jira.api.JIRALoginException;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.configuration.Server;

import java.util.List;

public interface JIRAServerFacade
{
    void testServerConnection(String url, String userName, String password) throws JIRALoginException;

    public List getAssignedIssues(Server server, String username) throws JIRAException;
}
