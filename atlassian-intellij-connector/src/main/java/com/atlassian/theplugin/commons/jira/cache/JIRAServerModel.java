package com.atlassian.theplugin.commons.jira.cache;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComponentBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAFixForVersionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAProject;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAResolutionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAVersionBean;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.Collection;
import java.util.List;

public interface JIRAServerModel {
	void clear(JiraServerData cfg);

	void clearAll();

	/*
	* returns false if invalid password or login occured
	 */
	Boolean checkServer(JiraServerData jiraServerData) throws RemoteApiException;

	String getErrorMessage(JiraServerData jiraServerData);

	List<JIRAProject> getProjects(JiraServerData jiraServerData) throws JIRAException;

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
}
