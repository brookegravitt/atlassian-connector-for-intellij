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
	boolean checkServer(JiraServerCfg cfg) throws RemoteApiException;

	String getErrorMessage(JiraServerCfg cfg);

	List<JIRAProject> getProjects(JiraServerCfg cfg);

	List<JIRAConstant> getStatuses(JiraServerCfg cfg);

	List<JIRAConstant> getIssueTypes(JiraServerCfg cfg, JIRAProject project);

	List<JIRAQueryFragment> getSavedFilters(JiraServerCfg cfg);

	List<JIRAConstant> getPriorities(JiraServerCfg cfg);

	List<JIRAResolutionBean> getResolutions(JiraServerCfg cfg);

	List<JIRAVersionBean> getVersions(JiraServerCfg cfg, JIRAProject project);

	List<JIRAFixForVersionBean> getFixForVersions(JiraServerCfg cfg, JIRAProject project);

	List<JIRAComponentBean> getComponents(JiraServerCfg cfg, JIRAProject project);


	Collection<JiraServerCfg> getServers();

	void clear(final ServerId serverId);

	void replace(final JiraServerCfg server);
}
