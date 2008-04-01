package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginException;

import java.util.List;

public class JIRAServerFacadeImpl implements JIRAServerFacade {
    private static final int MAX_ISSUES_DEFAULT = 50;

    public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
        JIRAXmlRpcClient client = new JIRAXmlRpcClient(url);
        try {
            if (!client.login(userName, password)) {
                throw new RemoteApiLoginException("Bad credentials");
            }
        } catch (JIRAException e) {
            throw new RemoteApiLoginException("Error logging in", e);
        }
    }

	public ServerType getServerType() {
		return ServerType.JIRA_SERVER;
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

	public List getIssueTypesForProject(Server server, String project) throws JIRAException {
		JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
		return client.getIssueTypesForProject(project);
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
