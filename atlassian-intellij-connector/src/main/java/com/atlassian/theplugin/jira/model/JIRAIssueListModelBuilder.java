package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.intellij.openapi.project.Project;

import java.util.Collection;
import java.util.List;

public interface JIRAIssueListModelBuilder extends FrozenModel {
	void setModel(JIRAIssueListModel model);

	JIRAIssueListModel getModel();

	void reloadIssue(String issueKey, JiraServerData jiraServerData) throws JIRAException;

	void updateIssue(final JiraIssueAdapter issue);

	void reset();

	void setProject(final Project project);

	void checkActiveIssue(Collection<JiraIssueAdapter> newIssues);

    void addIssuesToModel(List<JIRAQueryFragment> queryFragments,
                                              JiraServerData jiraServerCfg, int size,
                                              boolean reload) throws JIRAException;

    void addRecenltyOpenIssuesToModel(boolean reload);

    void addIssuesToModel(JIRAQueryFragment savedFilter,
                                              JiraServerData jiraServerCfg, int size,
                                              boolean reload) throws JIRAException;
}
