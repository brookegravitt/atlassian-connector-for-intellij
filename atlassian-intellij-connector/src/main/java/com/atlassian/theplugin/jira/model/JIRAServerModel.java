package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.*;

import java.util.List;

public interface JIRAServerModel {
	void clear(JiraServerCfg cfg);

	void clearAll();

	boolean checkServer(JiraServerCfg cfg);

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
}
