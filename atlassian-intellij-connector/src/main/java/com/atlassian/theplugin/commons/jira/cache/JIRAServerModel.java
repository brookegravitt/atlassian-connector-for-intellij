package com.atlassian.theplugin.commons.jira.cache;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.api.*;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Collection;
import java.util.List;

public interface JIRAServerModel {
	void clear(ServerData cfg);

	void clearAll();

	/*
	* returns false if invalid password or login occured
	 */
	Boolean checkServer(ServerData cfg) throws RemoteApiException;

	String getErrorMessage(ServerData cfg);

	List<JIRAProject> getProjects(ServerData cfg) throws JIRAException;

	List<JIRAConstant> getStatuses(ServerData cfg) throws JIRAException;

	List<JIRAConstant> getIssueTypes(ServerData cfg, JIRAProject project, boolean includeAny) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypes(ServerData cfg, JIRAProject project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(ServerData cfg) throws JIRAException;

	List<JIRAPriorityBean> getPriorities(ServerData cfg, boolean includeAny) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(ServerData cfg, boolean includeAnyAndUnknown) throws JIRAException;

	List<JIRAVersionBean> getVersions(ServerData cfg, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException;

	List<JIRAFixForVersionBean> getFixForVersions(ServerData cfg, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException;

	List<JIRAComponentBean> getComponents(ServerData cfg, JIRAProject project, final boolean includeSpecialValues)
			throws JIRAException;


	Collection<ServerData> getServers();

	void clear(final ServerId serverId);

	void replace(final ServerData server);
    boolean isChanged();
}
