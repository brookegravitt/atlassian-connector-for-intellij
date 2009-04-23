package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.jira.api.*;

import java.util.Collection;
import java.util.List;

public interface JIRAServerModel extends FrozenModel {
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

	List<JIRAConstant> getPriorities(ServerData cfg, boolean includeAny) throws JIRAException;

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
}
