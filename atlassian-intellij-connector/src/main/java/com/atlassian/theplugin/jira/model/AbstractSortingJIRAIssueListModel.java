package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.*;

public abstract class AbstractSortingJIRAIssueListModel implements JIRAIssueListModel {

	private final JIRAIssueListModel parent;

	public AbstractSortingJIRAIssueListModel(JIRAIssueListModel parent) {
		this.parent = parent;
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

	public void notifyListenersModelChanged() {
		parent.notifyListenersModelChanged();
	}

	public void notifyListenersIssuesLoaded(int numberOfLoadedIssues) {
		parent.notifyListenersIssuesLoaded(numberOfLoadedIssues);
	}

	public void addModelListener(JIRAIssueListModelListener listener) {
		parent.addModelListener(listener);
	}

	public void removeModelListener(JIRAIssueListModelListener listener) {
		parent.removeModelListener(listener);
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

}
