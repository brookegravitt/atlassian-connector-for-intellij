package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.intellij.openapi.project.Project;

import java.util.Collection;

public interface JIRAIssueListModelBuilder extends FrozenModel {
	void setModel(JIRAIssueListModel model);

	JIRAIssueListModel getModel();

	void addIssuesToModel(final JiraCustomFilter manualFilter,
                          final JiraServerData jiraServerData, int size, boolean reload)	throws JIRAException;

    void addIssuesToModel(final JiraPresetFilter presetFilter,
                          final JiraServerData jiraServerData, int size, boolean reload)	throws JIRAException;

	void addIssuesToModel(final JIRASavedFilter savedFilter,
                          final JiraServerData jiraServerData, int size, boolean reload)	throws JIRAException;

	void addRecenltyOpenIssuesToModel(boolean reload) throws JIRAException;

	void reloadIssue(String issueKey, JiraServerData jiraServerData) throws JIRAException;

	void updateIssue(final JiraIssueAdapter issue);

	void reset();

	void setProject(final Project project);

	void checkActiveIssue(Collection<JiraIssueAdapter> newIssues);
}
