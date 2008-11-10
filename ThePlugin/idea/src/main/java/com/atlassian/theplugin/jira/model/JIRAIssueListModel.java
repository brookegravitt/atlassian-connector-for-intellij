package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.List;

public interface JIRAIssueListModel {
	void clear();
	void addIssue(JIRAIssue issue);
	void addIssues(List<JIRAIssue> issues);
	List<JIRAIssue> getIssues();
	void notifyListeners();
	void addModelListener(JIRAIssueListModelListener listener);
	void removeModelListener(JIRAIssueListModelListener listener);

	void setSeletedIssue(JIRAIssue issue);
	JIRAIssue getSelectedIssue();
}
