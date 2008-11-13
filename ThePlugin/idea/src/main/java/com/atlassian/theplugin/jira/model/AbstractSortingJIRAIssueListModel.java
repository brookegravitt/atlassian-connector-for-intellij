package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.*;

public abstract class AbstractSortingJIRAIssueListModel implements JIRAIssueListModel, JIRAIssueListModelListener {

	private final JIRAIssueListModel parent;
	private List<JIRAIssueListModelListener> listeners;

	public AbstractSortingJIRAIssueListModel(JIRAIssueListModel parent) {
		this.parent = parent;
		listeners = new ArrayList<JIRAIssueListModelListener>();
		parent.addModelListener(this);
	}

	public void clear() {
		parent.clear();
	}

	public void addIssue(JIRAIssue issue) {
		parent.addIssue(issue);
	}

	public void addIssues(Collection<JIRAIssue> issues) {
		parent.addIssues(issues);
	}

	protected abstract Comparator<JIRAIssue> getComparator();

	public Collection<JIRAIssue> getIssues() {
		Collection<JIRAIssue> col = parent.getIssues();
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();
		for (JIRAIssue i : col) {
			list.add(i);
		}
		Collections.sort(list, getComparator());
		return list;
	}

	public void notifyListeners() {
		for (JIRAIssueListModelListener l : listeners) {
			l.modelChanged(this);
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
		parent.setSeletedIssue(issue);
	}

	public JIRAIssue getSelectedIssue() {
		return parent.getSelectedIssue();
	}

	public void setIssue(JIRAIssue issue) {
		parent.setIssue(issue);
	}

	public void modelChanged(JIRAIssueListModel model) {
		notifyListeners();
	}
}
