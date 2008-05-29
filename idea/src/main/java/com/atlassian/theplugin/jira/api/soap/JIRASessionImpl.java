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

package com.atlassian.theplugin.jira.api.soap;

import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.api.soap.axis.*;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.axis.AxisProperties;

public class JIRASessionImpl implements JIRASession {

	private String token;
	private JiraSoapService service;
	private String serverUrl;
	private URL portAddress;

	public static final int ONE_DAY_AGO = -24;

	private boolean loggedIn = false;

	//
	// AxisProperties are shit - if you try to set nonexistent property to null, NPE is thrown.
	//
	private void setAxisProperty(String name, String value) {
		if (value == null) {
			if (AxisProperties.getProperty(name) != null) {
				AxisProperties.setProperty(name, null);
			}
		} else {
			AxisProperties.setProperty(name, value);
		}
	}

	private void setProxy() {
		HttpConfigurableAdapter proxyInfo = ConfigurationFactory.getConfiguration().transientGetHttpConfigurable();
		String host = null;
		String port = null;
		String user = null;
		String password = null;
		if (proxyInfo.isUseHttpProxy()) {
			host = proxyInfo.getProxyHost();
			port = String.valueOf(proxyInfo.getProxyPort());
			if (proxyInfo.isProxyAuthentication()) {
				user = proxyInfo.getProxyLogin();
				password = proxyInfo.getPlainProxyPassword();
			}
		}

		//
		// well - re-setting proxy does not really work - Axis bug
		// see: http://issues.apache.org/jira/browse/AXIS-2295
		// So in order to apply new proxy settings, IDEA has to be restarted
		// all software sucks
		//
		setAxisProperty("http.proxyHost", host);
		setAxisProperty("http.proxyPort", port);
		setAxisProperty("https.proxyHost", host);
		setAxisProperty("https.proxyPort", port);
		setAxisProperty("http.proxyUser", user);
		setAxisProperty("https.proxyUser", user);
		setAxisProperty("http.proxyPassword", password);
		setAxisProperty("https.proxyPassword", password);
	}

	public JIRASessionImpl(String serverUrl) throws ServiceException, MalformedURLException {
		portAddress = new URL(serverUrl + "/rpc/soap/jirasoapservice-v2");
		JiraSoapServiceServiceLocator loc = new JiraSoapServiceServiceLocator();
		service = loc.getJirasoapserviceV2(portAddress);
		setProxy();

		this.serverUrl = serverUrl;
	}

	public void login(String userName, String password) throws RemoteApiLoginException {
		try {
			token = service.login(userName, password);
		} catch (RemoteAuthenticationException e) {
			throw new RemoteApiLoginException("Authentication failed");
		} catch (RemoteException e) {
	   		throw new RemoteApiLoginException(e.toString());
		}
		loggedIn = true;
	}

	public void logout() {
		try {
			if (service.logout(token)) {
				token = null;
				loggedIn = false;
			}
		} catch (java.rmi.RemoteException e) {
			// todo: log the exception
		}
	}

	public void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment) throws RemoteApiException {
		RemoteWorklog workLog = new RemoteWorklog();
		workLog.setStartDate(startDate);
		workLog.setTimeSpent(timeSpent);
		workLog.setComment(comment);
		try {
			service.addWorklogAndAutoAdjustRemainingEstimate(token, issue.getKey(), workLog);
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException {
		RemoteIssue remoteIssue = new RemoteIssue();

		remoteIssue.setProject(issue.getProjectKey());
		remoteIssue.setType(String.valueOf(issue.getTypeConstant().getId()));
		remoteIssue.setSummary(issue.getSummary());
		if (issue.getPriorityConstant().getId() != JIRAServer.ANY_ID) {
			remoteIssue.setPriority(String.valueOf(issue.getPriorityConstant().getId()));
		}

		if (issue.getDescription() != null) {
			remoteIssue.setDescription(issue.getDescription());
		}
		if (issue.getAssignee() != null) {
			remoteIssue.setAssignee(issue.getAssignee());
		}

		try {
			remoteIssue = service.createIssue(token, remoteIssue);
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}

		// todo: fill in all other fields. For now only the issue key and URL is being displayed
		JIRAIssueBean retVal = new JIRAIssueBean(serverUrl);
		retVal.setKey(remoteIssue.getKey());
		return retVal;
	}

	public void addComment(JIRAIssue issue, String comment) throws RemoteApiException {
		try {
			RemoteComment rComment = new RemoteComment();
			rComment.setBody(comment);
			service.addComment(token, issue.getKey(), rComment);
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAProject> getProjects() throws RemoteApiException {
		try {
			RemoteProject[] projects = service.getProjectsNoSchemes(token);
			List<JIRAProject> projectList = new ArrayList<JIRAProject>(projects.length);

			for (RemoteProject p : projects) {
				JIRAProjectBean project = new JIRAProjectBean();

				project.setName(p.getName());
				project.setKey(p.getKey());
				project.setDescription(p.getDescription());
				project.setUrl(p.getUrl());
				project.setLead(p.getLead());
				project.setId(Long.valueOf(p.getId()));

				projectList.add(project);
			}

			return projectList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	private List<JIRAConstant> issueTableToList(RemoteIssueType[] types) throws MalformedURLException {
		List<JIRAConstant> typesList = new ArrayList<JIRAConstant>();
		for (RemoteIssueType type : types) {
			typesList.add(new JIRAIssueTypeBean(Long.valueOf(type.getId()), type.getName(), new URL(type.getIcon())));
		}
		return typesList;
	}

	public List<JIRAConstant> getIssueTypes() throws RemoteApiException {
		try {
			return issueTableToList(service.getIssueTypes(token));
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (MalformedURLException e) {
			throw new RemoteApiException(e.toString(), e);
		}

	}

	public List<JIRAConstant> getIssueTypesForProject(String project) throws RemoteApiException {
		try {
			return issueTableToList(service.getIssueTypesForProject(token, project));
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (MalformedURLException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAConstant> getStatuses() throws RemoteApiException {
		try {
			RemoteStatus[] statuses = service.getStatuses(token);

			List<JIRAConstant> statusesList = new ArrayList<JIRAConstant>(statuses.length);
			for (RemoteStatus status : statuses) {
				statusesList.add(new JIRAStatusBean(
						Long.valueOf(status.getId()), status.getName(), new URL(status.getIcon())));
			}
			return statusesList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (MalformedURLException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAComponentBean> getComponents(String projectKey) throws RemoteApiException {
		try {
			RemoteComponent[] components = service.getComponents(token, projectKey);

			List<JIRAComponentBean> componentsList = new ArrayList<JIRAComponentBean>(components.length);
			for (RemoteComponent c : components) {
				componentsList.add(new JIRAComponentBean(Long.valueOf(c.getId()), c.getName()));
			}
			return componentsList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAVersionBean> getVersions(String projectKey) throws RemoteApiException {
		try {
			RemoteVersion[] versions = service.getVersions(token, projectKey);

			List<JIRAVersionBean> versionsList = new ArrayList<JIRAVersionBean>(versions.length);
			for (RemoteVersion v : versions) {
				versionsList.add(new JIRAVersionBean(Long.valueOf(v.getId()), v.getName()));
			}
			return versionsList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAConstant> getPriorities() throws RemoteApiException {
		try {
			RemotePriority[] priorities = service.getPriorities(token);

			List<JIRAConstant> prioritiesList = new ArrayList<JIRAConstant>(priorities.length);
			for (RemotePriority p : priorities) {
				prioritiesList.add(new JIRAPriorityBean(Long.valueOf(p.getId()), p.getName(), new URL(p.getIcon())));
			}
			return prioritiesList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (MalformedURLException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAResolutionBean> getResolutions() throws RemoteApiException {
		try {
			RemoteResolution[] resolutions = service.getResolutions(token);

			List<JIRAResolutionBean> resolutionsList = new ArrayList<JIRAResolutionBean>(resolutions.length);
			for (RemoteResolution p : resolutions) {
				resolutionsList.add(new JIRAResolutionBean(Long.valueOf(p.getId()), p.getName()));
			}
			return resolutionsList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException {
		try {
			RemoteFilter[] filters = service.getSavedFilters(token);

			List<JIRAQueryFragment> filtersList = new ArrayList<JIRAQueryFragment>(filters.length);
			for (RemoteFilter f : filters) {
				filtersList.add(new JIRASavedFilterBean(f.getName(), Long.valueOf(f.getId())));
			}
			return filtersList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}

	}

	public void setAssignee(JIRAIssue issue, String assignee) throws RemoteApiException {
		RemoteFieldValue v = new RemoteFieldValue();
		RemoteFieldValue[] vTable = {v};
		v.setId("assignee");
		v.setValues(new String[]{assignee});
		try {
			service.updateIssue(token, issue.getKey(), vTable);
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

    public List<JIRAAction> getAvailableActions(JIRAIssue issue) throws RemoteApiException {
        try {
            RemoteNamedObject[] actions = service.getAvailableActions(token, issue.getKey());
            List<JIRAAction> actionList = new ArrayList<JIRAAction>(actions.length);
            for (RemoteNamedObject action : actions) {
				actionList.add(new JIRAActionBean(Long.valueOf(action.getId()), action.getName()));
            }
            return actionList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

	public List<JIRAActionField> getFieldsForAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException {
		try {
			RemoteField[] fields = service.getFieldsForAction(
					token, issue.getKey(), Long.valueOf(action.getId()).toString());
			List<JIRAActionField> fieldList = new ArrayList<JIRAActionField>(fields.length);
			for (RemoteField f : fields) {
				fieldList.add(new JIRAActionFieldBean(f.getId(), f.getName()));
			}
			return fieldList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}
}
