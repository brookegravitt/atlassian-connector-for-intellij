package com.atlassian.theplugin.commons.jira.cache;

import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAFixForVersionBean;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.intellij.openapi.util.Pair;

import java.util.Collection;
import java.util.List;

public interface JIRAServerModel {
	void clear(JiraServerData cfg);

	void clearAll();

    void resetFacade();

    boolean isServerResponding(JiraServerData jiraServerData);

	/*
	* returns false if invalid password or login occured
	 */
	Boolean checkServer(JiraServerData jiraServerData) throws RemoteApiException;

	String getErrorMessage(JiraServerData jiraServerData);

    List<JIRAProject> getProjects(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAProject> getProjects(JiraServerData jiraServerData, boolean forIssueCreation) throws JIRAException;

	List<JIRAConstant> getStatuses(JiraServerData jiraServerData) throws JIRAException;

	List<JIRAConstant> getIssueTypes(JiraServerData jiraServerData, JIRAProject project, boolean includeAny)
            throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypes(JiraServerData jiraServerData, JIRAProject project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(JiraServerData jiraServerData) throws JIRAException;

	List<JIRAPriorityBean> getPriorities(JiraServerData jiraServerData, boolean includeAny) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(JiraServerData jiraServerData, boolean includeAnyAndUnknown)
            throws JIRAException;

	List<JIRAVersionBean> getVersions(JiraServerData jiraServerData, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException;

	List<JIRAFixForVersionBean> getFixForVersions(JiraServerData jiraServerData, JIRAProject project,
                                                  boolean includeSpecialValues)	throws JIRAException;

	List<JIRAComponentBean> getComponents(JiraServerData jiraServerData, JIRAProject project,
                                          final boolean includeSpecialValues) throws JIRAException;


	Collection<JiraServerData> getServers();

	void clear(final ServerId serverId);

	void replace(final JiraServerData jiraServerData);
    boolean isChanged();

    List<Pair<String, String>> getUsers(JiraServerData jiraServerData);

    void addUser(JiraServerData jiraServerData, String userId, String userName);

    List<JIRASecurityLevelBean> getSecurityLevels(JiraServerData jiraServerData, String key)
            throws RemoteApiException, JIRAException;
}
