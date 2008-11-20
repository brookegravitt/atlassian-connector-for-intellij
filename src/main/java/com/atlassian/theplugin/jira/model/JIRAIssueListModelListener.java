package com.atlassian.theplugin.jira.model;

public interface JIRAIssueListModelListener {
	void modelChanged(JIRAIssueListModel model);

	void issuesLoaded(JIRAIssueListModel model, int loadedIssues);
	void modelFrozen(JIRAIssueListModel model, boolean frozen);
}
