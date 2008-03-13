package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;

import java.util.List;

public class JIRAServerFacadeImpl implements JIRAServerFacade {
    private static final int MAX_ISSUES_DEFAULT = 50;

    public void testServerConnection(String url, String userName, String password) throws JIRALoginException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(url);
        try {
            if (!client.login(userName, password)) {
                throw new JIRALoginException("Bad credentials");
            }
        } catch (JIRAException e) {
            throw new JIRALoginException("Error logging in", e);
        }
    }

    public List getIssues(Server server, List<JIRAQueryFragment> query) throws JIRAException {
        JIRARssClient rss = new JIRARssClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return rss.getIssues(query, "updated", "DESC", MAX_ISSUES_DEFAULT);
    }

    public List getProjects(Server server) throws JIRAException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return client.getProjects();
    }

    public List<JIRAConstant> getIssueTypes(Server server) throws JIRAException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return client.getIssueTypes();
    }

    public List<JIRAConstant> getStatuses(Server server) throws JIRAException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return client.getStatuses();
    }

    public void addComment(Server server, JIRAIssue issue, String comment) throws JIRAException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        client.addIssueComment(issue.getKey(), comment);
    }

    public JIRAIssue createIssue(Server server, JIRAIssue issue) throws JIRAException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return client.createIssue(issue);
    }

    public List getComponents(Server server, String projectKey) throws JIRAException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return client.getComponents(projectKey);
    }

    public List getVersions(Server server, String projectKey) throws JIRAException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
        return client.getVersions(projectKey);
    }
}
