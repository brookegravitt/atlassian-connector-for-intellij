package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.*;

public final class JIRAIssueListModelImpl implements JIRAIssueListModel {

	private Map<String, JIRAIssue> issues;
	private List<JIRAIssueListModelListener> listeners;
	private JIRAIssue selectedIssue;

	private JIRAIssueListModelImpl() {
		issues = new HashMap<String, JIRAIssue>();
		listeners = new ArrayList<JIRAIssueListModelListener>();
	}

	public static JIRAIssueListModel createInstance() {
		return new JIRAIssueListModelImpl();
	}
	
	public void clear() {
		issues.clear();
	}

	public void addIssue(JIRAIssue issue) {
		issues.put(issue.getKey(), issue);
	}

	public void addIssues(Collection<JIRAIssue> list) {
		for (JIRAIssue i : list) {
			addIssue(i);
		}
	}

	public void setIssue(JIRAIssue issue) {
		issues.put(issue.getKey(), issue);
		if (selectedIssue != null && selectedIssue.getKey().equals(issue.getKey())) {
			selectedIssue  = issue;
		}
	}

	public Collection<JIRAIssue> getIssues() {
		return issues.values();
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
		if (issue != null && issues.containsValue(issue)) {
			selectedIssue = issue;
		} else {
			selectedIssue = null;
		}
	}

	public JIRAIssue getSelectedIssue() {
		return selectedIssue;
	}
}
