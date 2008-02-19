package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;

import java.util.List;

public interface JIRAServerFacade {
    void testServerConnection(String url, String userName, String password) throws JIRALoginException;

    List getIssues(Server server, List<JIRAQueryFragment> query) throws JIRAException;

    List<JIRAProject> getProjects(Server server) throws JIRAException;

    List<JIRAConstant> getIssueTypes(Server server) throws JIRAException;
    
    List<JIRAConstant> getStatuses(Server server) throws JIRAException;

    void addComment(Server server, JIRAIssue issue, String comment) throws JIRAException;

    JIRAIssue createIssue(Server server, JIRAIssue issue) throws JIRAException;
}
