package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface JIRAIssueListModel extends FrozenModel {
	void clear();

//	void addIssue(JIRAIssue issue);

	void addIssues(Collection<JiraIssueAdapter> issues);

	Collection<JiraIssueAdapter> getIssues();

	Collection<JiraIssueAdapter> getIssuesNoSubtasks();

	/**
	 * Returns list of subtasks of the issue
	 *
	 * @param parent - parent of subtasks. If null is passed, subtasks for issues that are not in the model are returned
	 * @return subtasks for the parent
	 */
	@NotNull
	Collection<JiraIssueAdapter> getSubtasks(JiraIssueAdapter parent);

	JiraIssueAdapter findIssue(String key);

//	void setSeletedIssue(JIRAIssue issue);

	void updateIssue(JiraIssueAdapter issue);

	void fireIssuesLoaded(int numberOfLoadedIssues);

	void fireIssueUpdated(final JiraIssueAdapter issue);

	void fireModelChanged();

	void addModelListener(JIRAIssueListModelListener listener);

	void removeModelListener(JIRAIssueListModelListener listener);

//	void clearCache();

//	Set<JIRAIssue> getIssuesCache();

//	JIRAIssue getIssueFromCache(IssueRecentlyOpenBean recentIssue);
}
