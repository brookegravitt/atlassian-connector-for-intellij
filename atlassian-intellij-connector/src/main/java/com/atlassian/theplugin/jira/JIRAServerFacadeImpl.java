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
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.api.soap.JIRASessionImpl;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class JIRAServerFacadeImpl implements JIRAServerFacade {

	private HttpSessionCallback callback;

	private JIRAServerFacadeImpl() {
		this.callback = new HttpSessionCallbackImpl();
	}

	public static synchronized JIRAServerFacade getInstance() {
		if (instance == null) {
			instance = new JIRAServerFacadeImpl();
		}
		return instance;
	}

	private Map<String, JIRARssClient> rssSessions = new WeakHashMap<String, JIRARssClient>();
	private Map<String, JIRASession> soapSessions = new WeakHashMap<String, JIRASession>();
	private static JIRAServerFacadeImpl instance;

	private String getSoapSessionKey(ServerData server) {
		return server.getUserName() + server.getUrl() + server.getPassword();
	}

	private synchronized JIRASession getSoapSession(ServerData server) throws RemoteApiException {
		String key = getSoapSessionKey(server);

		JIRASession session = soapSessions.get(key);
		if (session == null) {
			try {
				session = new JIRASessionImpl(server);
			} catch (MalformedURLException e) {
				throw new RemoteApiException(e);
			} catch (ServiceException e) {
				throw new RemoteApiException(e);
			}


			session.login(server.getUserName(), server.getPassword());
			soapSessions.put(key, session);
		}
		return session;
	}

	private synchronized JIRARssClient getRssSession(ServerData server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUserName() + server.getUrl() + server.getPassword();
		JIRARssClient session = rssSessions.get(key);
		if (session == null) {
			session = new JIRARssClient(server, callback);
			rssSessions.put(key, session);
		}
		return session;
	}

	public void testServerConnection(final ServerData serverCfg) throws RemoteApiException {
		testServerConnection(serverCfg, serverCfg.getUserName(), serverCfg.getPassword());
	}

	public void testServerConnection(ServerData server, String userName, String password) throws RemoteApiException {
		JIRASession session;
		try {
			session = new JIRASessionImpl(server);
		} catch (MalformedURLException e) {
			throw new RemoteApiException(e);
		} catch (ServiceException e) {
			throw new RemoteApiLoginException(e.getMessage(), e);
		}
		session.login(userName, password);
	}

	public ServerType getServerType() {
		return ServerType.JIRA_SERVER;
	}

	public List<JIRAIssue> getIssues(ServerData server,
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

	public List<JIRAIssue> getSavedFilterIssues(ServerData server,
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

	public JIRAIssue getIssue(ServerData server, String key) throws JIRAException {
		JIRARssClient rss;
		try {
			rss = getRssSession(server);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		return rss.getIssue(key);
	}

	public List<JIRAProject> getProjects(ServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getProjects();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getIssueTypes(ServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypes();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getIssueTypesForProject(ServerData server, String project) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getSubtaskIssueTypes(ServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSubtaskIssueTypes();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getSubtaskIssueTypesForProject(ServerData server, String project) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSubtaskIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}


	public List<JIRAConstant> getStatuses(ServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getStatuses();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void addComment(ServerData server, String issueKey, String comment) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.addComment(issueKey, comment);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAIssue createIssue(ServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			JIRAIssue i = soap.createIssue(issue);
			return getIssue(server, i.getKey());
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void logWork(ServerData server, JIRAIssue issue, String timeSpent, Calendar startDate,
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

	public List<JIRAComponentBean> getComponents(ServerData server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getComponents(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAVersionBean> getVersions(ServerData server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getVersions(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAPriorityBean> getPriorities(ServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getPriorities();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAResolutionBean> getResolutions(ServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getResolutions();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getSavedFilters(ServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSavedFilters();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAAction> getAvailableActions(ServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getAvailableActions(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAActionField> getFieldsForAction(ServerData server, JIRAIssue issue, JIRAAction action)
			throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getFieldsForAction(issue, action);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void setAssignee(ServerData server, JIRAIssue issue, String assignee) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.setAssignee(issue, assignee);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAComment> getComments(ServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getComments(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void progressWorkflowAction(ServerData server, JIRAIssue issue, JIRAAction action) throws JIRAException {
		progressWorkflowAction(server, issue, action, null);
	}

	public void progressWorkflowAction(ServerData server, JIRAIssue issue,
			JIRAAction action, List<JIRAActionField> fields) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.progressWorkflowAction(issue, action, fields);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAIssue getIssueDetails(ServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueDetails(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAUserBean getUser(ServerData server, String loginName) throws JIRAException, JiraUserNotFoundException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getUser(loginName);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}
}
