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

import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.api.soap.axis.*;
import com.atlassian.theplugin.jira.model.JIRAServerCache;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.axis.AxisProperties;
import org.xml.sax.SAXException;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JIRASessionImpl implements JIRASession {

	private String token;
	private JiraSoapService service;
	private ServerData server;
	private URL portAddress;

	public static final int ONE_DAY_AGO = -24;

	private boolean loggedIn;

	//
	// AxisProperties are shit - if you try to set nonexistent property to null, NPE is thrown. Moreover, sometimes
	// setting apparently *existing* property to null also throws NPE (see bug PL-412)! Crap, crap, crap...
	//
	private void setAxisProperty(String name, String value) {
		if (value == null) {
			if (AxisProperties.getProperty(name) != null) {
				try {
					AxisProperties.setProperty(name, "");
				} catch (NullPointerException e) {
					Logger.getInstance(getClass().getName()).info("Setting AXIS property " + name + " to empty", e);
				}
			}
		} else {
			AxisProperties.setProperty(name, value);
		}
	}

	private void setSystemProperty(String name, String value) {

		if (value == null) {
			//if (System.getProperty(name) != null) {
			try {
				System.setProperty(name, "");
			} catch (NullPointerException e) {
				Logger.getInstance(getClass().getName()).info("Setting system property " + name + " to empty", e);
			}
			//}
		} else {
			System.setProperty(name, value);
		}
	}

	private void setProxy() {
		boolean useIdeaProxySettings =
				ConfigurationFactory.getConfiguration().getGeneralConfigurationData().getUseIdeaProxySettings();
		HttpConfigurableAdapter proxyInfo = ConfigurationFactory.getConfiguration().transientGetHttpConfigurable();
		String host = null;
		String port = null;
		String user = null;
		String password = null;
		if (useIdeaProxySettings && proxyInfo.isUseHttpProxy()) {
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

		setSystemProperty("http.proxyHost", host);
		setSystemProperty("http.proxyPort", port);

		setAxisProperty("http.proxyUser", user);
		setSystemProperty("http.proxyUser", user);

		setAxisProperty("http.proxyPassword", password);
		setSystemProperty("http.proxyPassword", password);

	}

	public JIRASessionImpl(ServerData server) throws ServiceException, MalformedURLException {
		portAddress = new URL(server.getUrl() + "/rpc/soap/jirasoapservice-v2");
		JiraSoapServiceServiceLocator loc = new JiraSoapServiceServiceLocator();
		AbstractHttpSession.setUrl(portAddress); // dirty hack
		service = loc.getJirasoapserviceV2(portAddress);
		setProxy();

		this.server = server;
	}

	public void login(String userName, String password) throws RemoteApiException {
		try {
			token = service.login(userName, password);
		} catch (RemoteAuthenticationException e) {
			throw new RemoteApiLoginException("Authentication failed");
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString());
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

	public void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
			boolean updateEstimate, String newEstimate)
			throws RemoteApiException {
		RemoteWorklog workLog = new RemoteWorklog();
		workLog.setStartDate(startDate);
		workLog.setTimeSpent(timeSpent);
		if (comment != null) {
			workLog.setComment(comment);
		}
		try {
			if (updateEstimate) {
				if (newEstimate != null) {
					service.addWorklogWithNewRemainingEstimate(token, issue.getKey(), workLog, newEstimate);
				} else {
					service.addWorklogAndAutoAdjustRemainingEstimate(token, issue.getKey(), workLog);
				}
			} else {
				service.addWorklogAndRetainRemainingEstimate(token, issue.getKey(), workLog);
			}
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException {
		RemoteIssue remoteIssue = new RemoteIssue();

		remoteIssue.setProject(issue.getProjectKey());
		remoteIssue.setType(String.valueOf(issue.getTypeConstant().getId()));
		remoteIssue.setSummary(issue.getSummary());
		if (issue.getPriorityConstant().getId() != JIRAServerCache.ANY_ID) {
			remoteIssue.setPriority(String.valueOf(issue.getPriorityConstant().getId()));
		}

		if (issue.getDescription() != null) {
			remoteIssue.setDescription(issue.getDescription());
		}
		if (issue.getAssignee() != null) {
			remoteIssue.setAssignee(issue.getAssignee());
		}

		final List<JIRAConstant> components = issue.getComponents();
		if (components != null && components.size() > 0) {
			RemoteComponent[] remoteComponents = new RemoteComponent[components.size()];
			int i = 0;
			for (JIRAConstant component : components) {
				remoteComponents[i] = new RemoteComponent(String.valueOf(component.getId()), component.getName());
				i++;
			}
			remoteIssue.setComponents(remoteComponents);
		}

		try {
			remoteIssue = service.createIssue(token, remoteIssue);
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}

		// todo: fill in all other fields. For now only the issue key and URL is being displayed
		JIRAIssueBean retVal = new JIRAIssueBean(server);
		retVal.setKey(remoteIssue.getKey());
		return retVal;
	}

	public JIRAIssue getIssueDetails(JIRAIssue issue) throws RemoteApiException {
		RemoteSecurityLevel securityLevel = null;

		try {
			securityLevel = service.getSecurityLevel(token, issue.getKey());
		} catch (RemoteException e) {
			PluginUtil.getLogger().warn(
					"Soap method 'getSecurityLevel' thrown exception. "
							+ "Probably there is no 'SecurityLevel' on JIRA (non enterprise version of JIRA).", e);
		} catch (ClassCastException e) {
			PluginUtil.getLogger().warn(
                    "Soap method 'getSecurityLevel' thrown ClassCastException. Probably some JIRA error.", e);
		} catch (Exception e) {
            // PL-1492
            if (e instanceof SAXException) {
                PluginUtil.getLogger().warn(
                        "Soap method 'getSecurityLevel' thrown SAXException. Probably some JIRA error.", e);
            } else {
                throw new RemoteApiException(e);
            }
        }

		try {
			RemoteIssue rIssue = service.getIssue(token, issue.getKey());


			if (rIssue == null) {
				throw new RemoteApiException("Unable to retrieve issue details");
			}
			JIRAIssueBean issueBean = new JIRAIssueBean(issue);

			if (securityLevel != null) {
				issueBean.setSecurityLevel(
						new JIRASecurityLevelBean(Long.valueOf(securityLevel.getId()), securityLevel.getName()));
			}

			RemoteVersion[] aVers = rIssue.getAffectsVersions();
			List<JIRAConstant> av = new ArrayList<JIRAConstant>();
			for (RemoteVersion v : aVers) {
				av.add(new JIRAVersionBean(Long.valueOf(v.getId()), v.getName()));
			}
			issueBean.setAffectsVersions(av);

			RemoteVersion[] fVers = rIssue.getFixVersions();
			List<JIRAConstant> fv = new ArrayList<JIRAConstant>();
			for (RemoteVersion v : fVers) {
				fv.add(new JIRAVersionBean(Long.valueOf(v.getId()), v.getName()));
			}
			issueBean.setFixVersions(fv);

			RemoteComponent[] comps = rIssue.getComponents();
			List<JIRAConstant> c = new ArrayList<JIRAConstant>();
			for (RemoteComponent rc : comps) {
				c.add(new JIRAComponentBean(Long.valueOf(rc.getId()), rc.getName()));
			}
			issueBean.setComponents(c);

			issueBean.setProjectKey(rIssue.getProject());
			issueBean.setSummary(rIssue.getSummary());

			issueBean.setRawSoapIssue(rIssue);

			return issueBean;

		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (ClassCastException e) {
			throw new RemoteApiException(e.toString(), e);
		}

	}

	public void addComment(String issueKey, String comment) throws RemoteApiException {
		try {
			RemoteComment rComment = new RemoteComment();
			rComment.setBody(comment);
			service.addComment(token, issueKey, rComment);
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

	public List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException {
		try {
			return issueTableToList(service.getSubTaskIssueTypes(token));
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (MalformedURLException e) {
			throw new RemoteApiException(e.toString(), e);
		}

	}

	public List<JIRAConstant> getSubtaskIssueTypesForProject(String project) throws RemoteApiException {
		try {
			return issueTableToList(service.getSubTaskIssueTypesForProject(token, project));
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
			List<JIRAAction> actionList = new ArrayList<JIRAAction>(actions != null ? actions.length : 0);
			if (actions != null) {
				for (RemoteNamedObject action : actions) {
					actionList.add(new JIRAActionBean(Long.valueOf(action.getId()), action.getName()));
				}
			}
			return actionList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (ClassCastException e) {
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

	public void progressWorkflowAction(JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields)
			throws RemoteApiException {
		try {
			if (fields == null) {
				RemoteFieldValue[] dummyValues = new RemoteFieldValue[0];
				service.progressWorkflowAction(token, issue.getKey(), String.valueOf(action.getId()), dummyValues);
			} else {

				CopyOnWriteArrayList<JIRAActionField> safeFields = new CopyOnWriteArrayList<JIRAActionField>(fields);

				for (JIRAActionField field : safeFields) {
					if (field.getValues() == null) {
						safeFields.remove(field);
					}
				}

				int i = 0;
				RemoteFieldValue[] values = new RemoteFieldValue[safeFields.size()];

				for (JIRAActionField field : safeFields) {
					List<String> fieldValues = field.getValues();
					String[] fieldValueTable = fieldValues.toArray(new String[fieldValues.size()]);
					values[i] = new RemoteFieldValue(field.getFieldId(), fieldValueTable);
					++i;
				}
				service.progressWorkflowAction(token, issue.getKey(), String.valueOf(action.getId()), values);
			}
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public List<JIRAComment> getComments(JIRAIssue issue) throws RemoteApiException {
		try {
			RemoteComment[] comments = service.getComments(token, issue.getKey());
			if (comments == null) {
				throw new RemoteApiException("Unable to retrieve comments");
			}

			List<JIRAComment> commentsList = new ArrayList<JIRAComment>(comments.length);
			for (RemoteComment c : comments) {
				commentsList.add(new JIRACommentBean(c.getId(), c.getAuthor(), c.getBody(), c.getCreated()));
			}
			return commentsList;
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public JIRAUserBean getUser(String loginName) throws RemoteApiException, JiraUserNotFoundException {
		try {
			RemoteUser ru = service.getUser(token, loginName);
			if (ru == null) {
				throw new JiraUserNotFoundException("User Name for " + loginName + " not found");
			}
			return new JIRAUserBean(-1, ru.getFullname(), ru.getName()) {
				@Override
				public String getQueryStringFragment() {
					return null;
				}

				public JIRAQueryFragment getClone() {
					return null;
				}
			};
		} catch (RemoteException e) {
			throw new RemoteApiException(e.toString(), e);
		} catch (ClassCastException e) {
			throw new RemoteApiException(e.toString(), e);
		}
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}
}
