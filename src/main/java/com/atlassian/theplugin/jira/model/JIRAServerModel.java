package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.jira.api.*;

import java.util.Collection;
import java.util.List;

public interface JIRAServerModel extends FrozenModel {
	void clear(JiraServerCfg cfg);

	void clearAll();

	/*
	* returns false if invalid password or login occured
	 */
	Boolean checkServer(JiraServerCfg cfg) throws RemoteApiException;

	String getErrorMessage(JiraServerCfg cfg);

	List<JIRAProject> getProjects(JiraServerCfg cfg) throws JIRAException;

	List<JIRAConstant> getStatuses(JiraServerCfg cfg) throws JIRAException;

	List<JIRAConstant> getIssueTypes(JiraServerCfg cfg, JIRAProject project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(JiraServerCfg cfg) throws JIRAException;

	List<JIRAConstant> getPriorities(JiraServerCfg cfg) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(JiraServerCfg cfg) throws JIRAException;

	List<JIRAVersionBean> getVersions(JiraServerCfg cfg, JIRAProject project) throws JIRAException;

	List<JIRAFixForVersionBean> getFixForVersions(JiraServerCfg cfg, JIRAProject project) throws JIRAException;

	List<JIRAComponentBean> getComponents(JiraServerCfg cfg, JIRAProject project) throws JIRAException;


	Collection<JiraServerCfg> getServers();

	void clear(final ServerId serverId);

	void replace(final JiraServerCfg server);
}
