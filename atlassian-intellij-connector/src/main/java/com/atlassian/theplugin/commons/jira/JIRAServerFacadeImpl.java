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

package com.atlassian.theplugin.commons.jira;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallback;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JIRAComment;
import com.atlassian.theplugin.commons.jira.api.JIRAComponentBean;
import com.atlassian.theplugin.commons.jira.api.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.JIRAProject;
import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.JIRAResolutionBean;
import com.atlassian.theplugin.commons.jira.api.JIRAUserBean;
import com.atlassian.theplugin.commons.jira.api.JIRAVersionBean;
import com.atlassian.theplugin.commons.jira.api.JiraUserNotFoundException;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.api.rss.JIRARssClient;
import com.atlassian.theplugin.commons.jira.api.soap.JIRASession;
import com.atlassian.theplugin.commons.jira.api.soap.JIRASessionImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.Logger;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class JIRAServerFacadeImpl implements JIRAServerFacade {

	private final HttpSessionCallback callback;
	private static Logger logger;

	private JIRAServerFacadeImpl() {
		this.callback = new IntelliJHttpSessionCallback();
	}

	public static synchronized JIRAServerFacade getInstance() {
		if (instance == null) {
			instance = new JIRAServerFacadeImpl();
		}
		return instance;
	}

	private final Map<String, JIRARssClient> rssSessions = new WeakHashMap<String, JIRARssClient>();
	private final Map<String, JIRASession> soapSessions = new WeakHashMap<String, JIRASession>();
	private static JIRAServerFacadeImpl instance;

	private String getSoapSessionKey(JiraServerData server) {
		return server.getUsername() + server.getUrl() + server.getPassword();
	}

	private synchronized JIRASession getSoapSession(JiraServerData server) throws RemoteApiException {
		String key = getSoapSessionKey(server);

		JIRASession session = soapSessions.get(key);
		if (session == null) {
			try {
				session = new JIRASessionImpl(logger, server);
			} catch (MalformedURLException e) {
				throw new RemoteApiException(e);
			} catch (ServiceException e) {
				throw new RemoteApiException(e);
			}


			session.login(server.getUsername(), server.getPassword());
			soapSessions.put(key, session);
		}
		return session;
	}

	private synchronized JIRARssClient getRssSession(JiraServerData server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUsername() + server.getUrl() + server.getPassword();
		JIRARssClient session = rssSessions.get(key);
		if (session == null) {
			session = new JIRARssClient(server, callback);
			rssSessions.put(key, session);
		}
		return session;
	}

	public void testServerConnection(final ConnectionCfg serverCfg) throws RemoteApiException {
		testServerConnection(new JiraServerData(new Server() {
			public boolean isUseDefaultCredentials() {
				return false;
			}
			
			public boolean isEnabled() {
				return true;
			}
			
			public String getUsername() {
				return serverCfg.getUsername();
			}
			
			public String getUrl() {
				return serverCfg.getUrl();
			}
			
			public ServerType getServerType() {
				return ServerType.JIRA_SERVER;
			}
			
			public ServerIdImpl getServerId() {
				return new ServerIdImpl(serverCfg.getId());
			}
			
			public String getPassword() {
				return serverCfg.getPassword();
			}
			
			public String getName() {
				return "unknown name";
			}


		}, new UserCfg(), false) , serverCfg.getUsername(), serverCfg.getPassword());
	}

	private void testServerConnection(JiraServerData server, String userName, String password) throws RemoteApiException {
		JIRASession session;
		try {
			session = new JIRASessionImpl(logger, server);
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

	public static void setLogger(Logger logger) {
		JIRAServerFacadeImpl.logger = logger;
	}

	public List<JIRAIssue> getIssues(JiraServerData server,
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

	public List<JIRAIssue> getSavedFilterIssues(JiraServerData server,
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

	public JIRAIssue getIssue(JiraServerData server, String key) throws JIRAException {
		JIRARssClient rss;
		try {
			rss = getRssSession(server);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		return rss.getIssue(key);
	}

	public List<JIRAProject> getProjects(JiraServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getProjects();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getIssueTypes(JiraServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypes();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getIssueTypesForProject(JiraServerData server, String project) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getSubtaskIssueTypes(JiraServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSubtaskIssueTypes();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData server, String project) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSubtaskIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}


	public List<JIRAConstant> getStatuses(JiraServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getStatuses();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void addComment(JiraServerData server, String issueKey, String comment) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.addComment(issueKey, comment);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAIssue createIssue(JiraServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			JIRAIssue i = soap.createIssue(issue);
			return getIssue(server, i.getKey());
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void logWork(JiraServerData server, JIRAIssue issue, String timeSpent, Calendar startDate,
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

	public List<JIRAComponentBean> getComponents(JiraServerData server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getComponents(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAVersionBean> getVersions(JiraServerData server, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getVersions(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			if (e == null) {
				logger.warn("PL-1710: e is null");
			} else if (e.getMessage() == null) {
				logger.warn("PL-1710: e.getMessage() is null");
			}
//			if (e == null || e.getMessage() == null) {
//				throw new JIRAException("Cannot retrieve versions from the server", e);
//			}
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAPriorityBean> getPriorities(JiraServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getPriorities();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAResolutionBean> getResolutions(JiraServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getResolutions();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getSavedFilters(JiraServerData server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getSavedFilters();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAAction> getAvailableActions(JiraServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getAvailableActions(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAActionField> getFieldsForAction(JiraServerData server, JIRAIssue issue, JIRAAction action)
			throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getFieldsForAction(issue, action);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void setAssignee(JiraServerData server, JIRAIssue issue, String assignee) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.setAssignee(issue, assignee);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAComment> getComments(JiraServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getComments(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void progressWorkflowAction(JiraServerData server, JIRAIssue issue, JIRAAction action) throws JIRAException {
		progressWorkflowAction(server, issue, action, null);
	}

	public void progressWorkflowAction(JiraServerData server, JIRAIssue issue,
			JIRAAction action, List<JIRAActionField> fields) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			soap.progressWorkflowAction(issue, action, fields);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAIssue getIssueDetails(JiraServerData server, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getIssueDetails(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAUserBean getUser(JiraServerData server, String loginName) throws JIRAException, JiraUserNotFoundException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getUser(loginName);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}
}
