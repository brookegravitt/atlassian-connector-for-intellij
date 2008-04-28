/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.api.soap.JIRASessionImpl;
import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginException;

import javax.xml.rpc.ServiceException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class JIRAServerFacadeImpl implements JIRAServerFacade {
	private Map<String, JIRARssClient> sessions = new WeakHashMap<String, JIRARssClient>();
	private Map<String, JIRASession> soapSessions = new WeakHashMap<String, JIRASession>();

	private synchronized JIRASession getSoapSession(Server server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUserName() + server.getUrlString() + server.getPasswordString();
		JIRASession session = soapSessions.get(key);
		if (session == null) {
			try {
				session = new JIRASessionImpl(new URL(server.getUrlString() + "/rpc/soap/jirasoapservice-v2"));
				session.login(server.getUserName(), server.getPasswordString());
			} catch (MalformedURLException e) {
				throw new RemoteApiException(e);
			} catch (ServiceException e) {
				throw new RemoteApiException(e);
			}
			soapSessions.put(key, session);
		}
		return session;
	}

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

        try {

			JIRAXmlRpcClient client = new JIRAXmlRpcClient(url);

			if (!client.login(userName, password)) {
                throw new RemoteApiLoginException("Bad credentials");
            }
        } catch (JIRAException e) {
            throw new RemoteApiLoginException(e.getMessage(), e);
        } 
	}

	public ServerType getServerType() {
		return ServerType.JIRA_SERVER;
	}

	public List getIssues(Server server,
						  List<JIRAQueryFragment> query,
						  String sort,
						  String sortOrder,
						  int start,
						  int size) throws JIRAException {
		JIRARssClient rss = null;
		try {
			rss = getSession(server);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		return rss.getIssues(query, sort, sortOrder, start, size);
    }

	public List getSavedFilterIssues(Server server,
									 List<JIRAQueryFragment> query,
									 String sort,
									 String sortOrder,
									 int start,
									 int size) throws JIRAException {
		JIRARssClient rss;
		try {
			rss = getSession(server);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		if (query.size() != 1) {
			throw new JIRAException("Only one saved filter could be used for query");
		} else {
			return rss.getSavedFilterIssues(query.get(0), sort, sortOrder, start, size);
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

	public void logWork(Server server, JIRAIssue issue, String timeSpent, String comment) throws JIRAException {
		JIRASession soap;
		try {
			soap = getSoapSession(server);
			soap.logWork(issue, timeSpent, comment);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		//To change body of implemented methods use File | Settings | File Templates.
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
