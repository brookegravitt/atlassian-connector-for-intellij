package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;

/**
 * IF YOU IMPLEMENT THE INTERFACE REMEMBER THAT:
 * Methods should be called in the order listed below!!!
 */
public interface JIRAIssueListModelListener {
	void issueUpdated(final JIRAIssue issue);

	/**
	 * That method should be called always whenever model has changed
	 * Other methods can be called as well
	 *
	 * @param model fresh model
	 */
	void modelChanged(JIRAIssueListModel model);

	void issuesLoaded(JIRAIssueListModel model, int loadedIssues);
}
