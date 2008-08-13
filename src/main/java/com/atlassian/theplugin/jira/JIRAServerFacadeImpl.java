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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.api.soap.JIRASessionImpl;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class JIRAServerFacadeImpl implements JIRAServerFacade {
	private JIRAServerFacadeImpl() {

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

	private String getSoapSessionKey(JiraServerCfg server) {
		return server.getUsername() + server.getUrl() + server.getPassword();
	}

	private synchronized JIRASession getSoapSession(JiraServerCfg server) throws RemoteApiException {
		String key = getSoapSessionKey(server);

		JIRASession session = soapSessions.get(key);
		if (session == null) {
			try {
				session = new JIRASessionImpl(server.getUrl());
				session.login(server.getUsername(), server.getPassword());
			} catch (MalformedURLException e) {
				throw new RemoteApiException(e);
			} catch (ServiceException e) {
				throw new RemoteApiException(e);
			}
			soapSessions.put(key, session);
		}
		return session;
	}

	private synchronized JIRARssClient getRssSession(JiraServerCfg server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUsername() + server.getUrl() + server.getPassword();
		JIRARssClient session = rssSessions.get(key);
		if (session == null) {
			session = new JIRARssClient(server.getUrl(), server.getUsername(), server.getPassword());
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

	public List<JIRAIssue> getIssues(JiraServerCfg server,
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

	public List<JIRAIssue> getSavedFilterIssues(JiraServerCfg server,
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

	public List<JIRAProject> getProjects(JiraServerCfg server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getProjects();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
    }

    public List<JIRAConstant> getIssueTypes(JiraServerCfg server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypes();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
    }

	public List<JIRAConstant> getIssueTypesForProject(JiraServerCfg server, String project) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

    public List<JIRAConstant> getStatuses(JiraServerCfg server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getStatuses();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
    }

	public void addComment(JiraServerCfg server, JIRAIssue issue, String comment) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.addComment(issue, comment);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
    }

    public JIRAIssue createIssue(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.createIssue(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
    }

	public void logWork(JiraServerCfg server, JIRAIssue issue, String timeSpent, Calendar startDate,
						String comment, boolean updateEstimate, String newEstimate)
            throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.logWork(issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAComponentBean> getComponents(JiraServerCfg server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getComponents(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
    }

    public List<JIRAVersionBean> getVersions(JiraServerCfg server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getVersions(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getPriorities(JiraServerCfg server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getPriorities();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAResolutionBean> getResolutions(JiraServerCfg server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getResolutions();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getSavedFilters(JiraServerCfg server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSavedFilters();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

    public List<JIRAAction> getAvailableActions(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(server);
            return soap.getAvailableActions(issue);
        } catch (RemoteApiException e) {
            soapSessions.remove(getSoapSessionKey(server));
            throw new JIRAException(e.getMessage(), e);
        }
    }

	public List<JIRAActionField> getFieldsForAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action)
			throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getFieldsForAction(issue, action);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void setAssignee(JiraServerCfg server, JIRAIssue issue, String assignee) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.setAssignee(issue, assignee);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAComment> getComments(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getComments(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void progressWorkflowAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.progressWorkflowAction(issue, action);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAIssue getIssueDetails(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueDetails(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}
}
