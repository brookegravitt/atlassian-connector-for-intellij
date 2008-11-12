package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.List;
import java.util.ArrayList;

public final class JIRAIssueListModelImpl implements JIRAIssueListModel {

	private List<JIRAIssue> issues;
	private List<JIRAIssueListModelListener> listeners;
	private JIRAIssue selectedIssue;

	private JIRAIssueListModelImpl() {
		issues = new ArrayList<JIRAIssue>();
		listeners = new ArrayList<JIRAIssueListModelListener>();
	}

	public static JIRAIssueListModel createInstance() {
		return new JIRAIssueListModelImpl();
	}
	
	public void clear() {
		issues.clear();
	}

	public void addIssue(JIRAIssue issue) {
		issues.add(issue);
	}

	public void addIssues(List<JIRAIssue> list) {
		for (JIRAIssue i : list) {
			addIssue(i);
		}
	}

	public List<JIRAIssue> getIssues() {
		return issues;
	}

	public void notifyListeners() {
		for (JIRAIssueListModelListener listener : listeners) {
			listener.modelChanged(this);
		}
	}

	public void addModelListener(JIRAIssueListModelListener listener) {
		listeners.add(listener);
	}

	public void removeModelListener(JIRAIssueListModelListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void setSeletedIssue(JIRAIssue issue) {
		if (issue != null && issues.contains(issue)) {
			selectedIssue = issue;
		} else {
			selectedIssue = null;
		}
	}

	public JIRAIssue getSelectedIssue() {
		return selectedIssue;
	}
}
