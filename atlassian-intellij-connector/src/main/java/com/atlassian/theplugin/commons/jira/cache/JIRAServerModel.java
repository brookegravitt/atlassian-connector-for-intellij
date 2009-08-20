package com.atlassian.theplugin.commons.jira.cache;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.api.*;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Collection;
import java.util.List;

public interface JIRAServerModel {
	void clear(JiraServerData cfg);

	void clearAll();

	/*
	* returns false if invalid password or login occured
	 */
	Boolean checkServer(JiraServerData cfg) throws RemoteApiException;

	String getErrorMessage(JiraServerData cfg);

	List<JIRAProject> getProjects(JiraServerData cfg) throws JIRAException;

	List<JIRAConstant> getStatuses(JiraServerData cfg) throws JIRAException;

	List<JIRAConstant> getIssueTypes(JiraServerData cfg, JIRAProject project, boolean includeAny) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypes(JiraServerData cfg, JIRAProject project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(JiraServerData cfg) throws JIRAException;

	List<JIRAPriorityBean> getPriorities(JiraServerData cfg, boolean includeAny) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(JiraServerData cfg, boolean includeAnyAndUnknown) throws JIRAException;

	List<JIRAVersionBean> getVersions(JiraServerData cfg, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException;

	List<JIRAFixForVersionBean> getFixForVersions(JiraServerData cfg, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException;

	List<JIRAComponentBean> getComponents(JiraServerData cfg, JIRAProject project, final boolean includeSpecialValues)
			throws JIRAException;


	Collection<JiraServerData> getServers();

	void clear(final ServerId serverId);

	void replace(final JiraServerData server);
    boolean isChanged();
}
