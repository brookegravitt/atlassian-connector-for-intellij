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
	private JIRAServerFacadeImpl(){

	}

	public static JIRAServerFacade getInstance() {
		if (instance == null) {
			instance = new JIRAServerFacadeImpl();
		}
		return instance;
	}

	private Map<String, JIRARssClient> rssSessions = new WeakHashMap<String, JIRARssClient>();
	private Map<String, JIRASession> soapSessions = new WeakHashMap<String, JIRASession>();
	private static JIRAServerFacadeImpl instance;

	private synchronized JIRASession getSoapSession(Server server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUserName() + server.getUrlString() + server.getPasswordString();
		JIRASession session = soapSessions.get(key);
		if (session == null) {
			try {
				session = new JIRASessionImpl(server.getUrlString());
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

	private synchronized JIRARssClient getRssSession(Server server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUserName() + server.getUrlString() + server.getPasswordString();
		JIRARssClient session = rssSessions.get(key);
		if (session == null) {
			session = new JIRARssClient(server.getUrlString(), server.getUserName(), server.getPasswordString());
			rssSessions.put(key, session);
		}
		return session;
	}

	public void testServerConnection(String url, String userName, String password) throws RemoteApiException {

        try {
			JIRASession session = new JIRASessionImpl(url);
			session.login(userName, password);
		} catch (MalformedURLException e) {
			throw new RemoteApiException(e);
		} catch (ServiceException e) {
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
		JIRARssClient rss;
		try {
			rss = getRssSession(server);
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
			rss = getRssSession(server);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		if (query.size() != 1) {
			throw new JIRAException("Only one saved filter could be used for query");
		} else {
			return rss.getSavedFilterIssues(query.get(0), sort, sortOrder, start, size);
		}
	}

	public List<JIRAProject> getProjects(Server server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getProjects();
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
    }

    public List<JIRAConstant> getIssueTypes(Server server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypes();
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
    }

	public List<JIRAConstant> getIssueTypesForProject(Server server, String project) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

    public List<JIRAConstant> getStatuses(Server server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getStatuses();
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
    }

	public void addComment(Server server, JIRAIssue issue, String comment) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.addComment(issue, comment);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
    }

    public JIRAIssue createIssue(Server server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.createIssue(issue);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
    }

	public void logWork(Server server, JIRAIssue issue, String timeSpent, String comment) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.logWork(issue, timeSpent, comment);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getComponents(Server server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getComponents(projectKey);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
    }

    public List<JIRAVersionBean> getVersions(Server server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getVersions(projectKey);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getPriorities(Server server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getPriorities();
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getResolutions(Server server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getResolutions();
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getSavedFilters(Server server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSavedFilters();
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}
}
