package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.intellij.openapi.project.Project;

public interface JIRAIssueListModelBuilder extends FrozenModel {
	void setModel(JIRAIssueListModel model);

	JIRAIssueListModel getModel();

	void addIssuesToModel(final JIRAManualFilter manualFilter, final JiraServerCfg jiraServerCfg, int size, boolean reload)
			throws JIRAException;

	void addIssuesToModel(final JIRASavedFilter savedFilter, final JiraServerCfg jiraServerCfg, int size, boolean reload)
			throws JIRAException;

	void addRecenltyOpenIssuesToModel(/*RecentlyOpenIssuesCache recentlyOpenIssues,*/ boolean reload) throws JIRAException;

	void updateIssue(JIRAIssue issue, JiraServerCfg jiraServerCfg) throws JIRAException;

	void reset();

//	JIRAIssue getJIRAIssue(IssueRecentlyOpenBean recentlyOpen) throws JIRAException;

	void setProject(final Project project);

	void setProjectCfgManager(final ProjectCfgManager projectCfgManager);
//	void clearCache();

}
