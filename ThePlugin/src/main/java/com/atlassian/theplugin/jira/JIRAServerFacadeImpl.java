package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginException;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class JIRAServerFacadeImpl implements JIRAServerFacade {
    private static final int MAX_ISSUES_DEFAULT = 50;

	private Map<String, JIRARssClient> sessions = new WeakHashMap<String, JIRARssClient>();


	private synchronized JIRARssClient getSession(Server server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUserName() + server.getUrlString() + server.getPasswordString();
		JIRARssClient session = sessions.get(key);
		if (session == null) {
			session = new JIRARssClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
			sessions.put(key, session);
		}
		return session;
	}


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
		JIRARssClient rss = null;
		try {
			rss = getSession(server);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		return rss.getIssues(query, "updated", "DESC", MAX_ISSUES_DEFAULT);
    }

	public List getSavedFilterIssues(Server server, List<JIRAQueryFragment> query) throws JIRAException {
		JIRARssClient rss = null;
		try {
			rss = getSession(server);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		if (query.size() != 1) {
			throw new JIRAException("Only one saved filter could be used for query");
		} else {
			return rss.getSavedFilterIssues(query.get(0), "updated", "DESC", MAX_ISSUES_DEFAULT);
		}
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

	public List getPriorieties(Server server) throws JIRAException {
		JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
		return client.getPriorities();
	}

	public List getResolutions(Server server) throws JIRAException {
		JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
		return client.getResolutions();
	}

	public List getSavedFilters(Server server) throws JIRAException {
		JIRAXmlRpcClient client = new JIRAXmlRpcClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
		return client.getSavedFilters();		
	}
}
