package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.intellij.openapi.project.Project;

import java.util.Collection;

public interface JIRAIssueListModelBuilder extends FrozenModel {
	void setModel(JIRAIssueListModel model);

	JIRAIssueListModel getModel();

	void addIssuesToModel(final JIRAManualFilter manualFilter, final ServerData jiraServerCfg, int size, boolean reload)
			throws JIRAException;

	void addIssuesToModel(final JIRASavedFilter savedFilter, final ServerData jiraServerCfg, int size, boolean reload)
			throws JIRAException;

	void addRecenltyOpenIssuesToModel(boolean reload) throws JIRAException;

	void reloadIssue(String issueKey, ServerData jiraServerCfg) throws JIRAException;

	void updateIssue(final JIRAIssue issue);

	void reset();

	void setProject(final Project project);

	void setProjectCfgManager(final ProjectCfgManagerImpl projectCfgManager);

	void checkActiveIssue(Collection<JIRAIssue> newIssues);
}
