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
import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.connector.commons.jira.JIRAAction;
import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.JiraUserNotFoundException;
import com.atlassian.connector.commons.jira.beans.JIRAAttachment;
import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.connector.commons.jira.rss.JIRARssClient;
import com.atlassian.connector.commons.jira.soap.JIRASession;
import com.atlassian.connector.commons.jira.soap.JIRASessionImpl;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.Logger;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class JIRAServerFacade2Impl implements JIRAServerFacade2 {

	private final HttpSessionCallback callback;
	private static Logger logger;

	private final Map<String, JIRARssClient> rssSessions = new WeakHashMap<String, JIRARssClient>();
	private final Map<String, JIRASession> soapSessions = new WeakHashMap<String, JIRASession>();

	private String getSoapSessionKey(HttpConnectionCfg httpConnectionCfg) {
		return httpConnectionCfg.getUsername() + httpConnectionCfg.getUrl() + httpConnectionCfg.getPassword();
	}


    public JIRAServerFacade2Impl(HttpSessionCallback callback) {
        this.callback = callback;
    }

    private synchronized JIRASession getSoapSession(HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
		String key = getSoapSessionKey(httpConnectionCfg);

		JIRASession session = soapSessions.get(key);
		if (session == null) {
			try {
				session = new JIRASessionImpl(logger, httpConnectionCfg);
			} catch (MalformedURLException e) {
				throw new RemoteApiException(e);
			} catch (ServiceException e) {
				throw new RemoteApiException(e);
			}


			session.login(httpConnectionCfg.getUsername(), httpConnectionCfg.getPassword());
			soapSessions.put(key, session);
		}
		return session;
	}

	private synchronized JIRARssClient getRssSession(HttpConnectionCfg server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUsername() + server.getUrl() + server.getPassword();
		JIRARssClient session = rssSessions.get(key);
		if (session == null) {
			session = new JIRARssClient(server, callback);
			rssSessions.put(key, session);
		}
		return session;
	}

	public void testServerConnection(final HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
		testServerConnection(httpConnectionCfg, httpConnectionCfg.getUsername(), httpConnectionCfg.getPassword());
	}

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
        //shouldn't be used
    }

    private void testServerConnection(HttpConnectionCfg httpConnectionCfg, String userName, String password)
            throws RemoteApiException {
		JIRASession session;
		try {
			session = new JIRASessionImpl(logger, httpConnectionCfg);
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
		JIRAServerFacade2Impl.logger = logger;
	}

	public List<JIRAIssue> getIssues(HttpConnectionCfg httpConnectionCfg,
			List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException {
		JIRARssClient rss;
		try {
			rss = getRssSession(httpConnectionCfg);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		return rss.getIssues(query, sort, sortOrder, start, size);
	}

	public List<JIRAIssue> getSavedFilterIssues(HttpConnectionCfg httpConnectionCfg,
			List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException {
		JIRARssClient rss;
		try {
			rss = getRssSession(httpConnectionCfg);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		if (query.size() != 1) {
			throw new JIRAException("Only one saved filter could be used for query");
		} else {
			return rss.getSavedFilterIssues(query.get(0), sort, sortOrder, start, size);
		}
	}

	public JIRAIssue getIssue(HttpConnectionCfg httpConnectionCfg, String key) throws JIRAException {
		JIRARssClient rss;
		try {
			rss = getRssSession(httpConnectionCfg);
		} catch (RemoteApiException e) {
			throw new JIRAException(e.getMessage(), e);
		}
		return rss.getIssue(key);
	}

	public List<JIRAProject> getProjects(HttpConnectionCfg server) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(server);
			return soap.getProjects();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(server));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getIssueTypes(HttpConnectionCfg httpConnectionCfg) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getIssueTypes();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getIssueTypesForProject(HttpConnectionCfg httpConnectionCfg, String project)
            throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getSubtaskIssueTypes(HttpConnectionCfg httpConnectionCfg) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getSubtaskIssueTypes();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAConstant> getSubtaskIssueTypesForProject(HttpConnectionCfg httpConnectionCfg, String project)
            throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getSubtaskIssueTypesForProject(project);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}


	public List<JIRAConstant> getStatuses(HttpConnectionCfg connection) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(connection);
			return soap.getStatuses();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(connection));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void addComment(HttpConnectionCfg httpConnectionCfg, String issueKey, String comment) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			soap.addComment(issueKey, comment);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAIssue createIssue(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			JIRAIssue i = soap.createIssue(issue);
			return getIssue(httpConnectionCfg, i.getKey());
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void logWork(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, String timeSpent, Calendar startDate,
			String comment, boolean updateEstimate, String newEstimate)
			throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			soap.logWork(issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAComponentBean> getComponents(HttpConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getComponents(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAVersionBean> getVersions(HttpConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getVersions(projectKey);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
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

	public List<JIRAPriorityBean> getPriorities(HttpConnectionCfg httpConnectionCfg) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getPriorities();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAResolutionBean> getResolutions(HttpConnectionCfg httpConnectionCfg) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getResolutions();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAQueryFragment> getSavedFilters(HttpConnectionCfg httpConnectionCfg) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getSavedFilters();
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAAction> getAvailableActions(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getAvailableActions(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAActionField> getFieldsForAction(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action)
			throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getFieldsForAction(issue, action);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public void setAssignee(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, String assignee) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			soap.setAssignee(issue, assignee);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAComment> getComments(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getComments(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

    public Collection<JIRAAttachment> getIssueAttachements(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(httpConnectionCfg);
            return soap.getIssueAttachements(issue);
        } catch (RemoteApiException e) {
            soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void progressWorkflowAction(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException {
		progressWorkflowAction(httpConnectionCfg, issue, action, null);
	}

	public void progressWorkflowAction(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue,
			JIRAAction action, List<JIRAActionField> fields) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			soap.progressWorkflowAction(issue, action, fields);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAIssue getIssueDetails(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getIssueDetails(issue);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}

	public JIRAUserBean getUser(HttpConnectionCfg httpConnectionCfg, String loginName)
            throws JIRAException, JiraUserNotFoundException {
		try {
			JIRASession soap = getSoapSession(httpConnectionCfg);
			return soap.getUser(loginName);
		} catch (RemoteApiException e) {
			soapSessions.remove(getSoapSessionKey(httpConnectionCfg));
			throw new JIRAException(e.getMessage(), e);
		}
	}
}
