package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface JIRAIssueListModel extends FrozenModel {
	void clear();

//	void addIssue(JIRAIssue issue);

	void addIssues(Collection<JIRAIssue> issues);

	Collection<JIRAIssue> getIssues();

	Collection<JIRAIssue> getIssuesNoSubtasks();

	/**
	 * Returns list of subtasks of the issue
	 *
	 * @param parent - parent of subtasks. If null is passed, subtasks for issues that are not in the model are returned
	 * @return subtasks for the parent
	 */
	@NotNull
	Collection<JIRAIssue> getSubtasks(JIRAIssue parent);

	JIRAIssue findIssue(String key);

//	void setSeletedIssue(JIRAIssue issue);

	void updateIssue(JIRAIssue issue);

	void fireIssuesLoaded(int numberOfLoadedIssues);

	void fireIssueUpdated(final JIRAIssue issue);

	void fireModelChanged();

	void addModelListener(JIRAIssueListModelListener listener);

	void removeModelListener(JIRAIssueListModelListener listener);

//	void clearCache();

//	Set<JIRAIssue> getIssuesCache();

//	JIRAIssue getIssueFromCache(IssueRecentlyOpenBean recentIssue);
}
