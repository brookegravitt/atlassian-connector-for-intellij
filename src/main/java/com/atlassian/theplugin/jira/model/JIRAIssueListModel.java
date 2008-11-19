package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.Collection;

public interface JIRAIssueListModel {
	void clear();
	void addIssue(JIRAIssue issue);
	void addIssues(Collection<JIRAIssue> issues);
	Collection<JIRAIssue> getIssues();

	void setSeletedIssue(JIRAIssue issue);
	JIRAIssue getSelectedIssue();

	void setIssue(JIRAIssue issue);

	void fireIssuesLoaded(int numberOfLoadedIssues);
	void fireModelChanged();
	void addModelListener(JIRAIssueListModelListener listener);
	void removeModelListener(JIRAIssueListModelListener listener);
}
