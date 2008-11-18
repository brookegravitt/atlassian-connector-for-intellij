package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchingJIRAIssueListModel implements JIRAIssueListModel, JIRAIssueListModelListener {
	private final JIRAIssueListModel parent;
	private List<JIRAIssueListModelListener> listeners;

	private String searchTerm;

	public SearchingJIRAIssueListModel(JIRAIssueListModel parent) {
		this.parent = parent;
		searchTerm = "";
		listeners = new ArrayList<JIRAIssueListModelListener>();
		parent.addModelListener(this);
	}

	public void setSearchTerm(@NotNull String searchTerm) {
		if (this.searchTerm.equals(searchTerm)) {
			return;
		}
		this.searchTerm = searchTerm.toLowerCase();
		notifyListenersModelChanged();
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

	public Collection<JIRAIssue> getIssues() {
		Collection<JIRAIssue> col = parent.getIssues();
		if (searchTerm.length() == 0) {
			return col;
		}
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();
		for (JIRAIssue i : col) {
			if (i.getKey().toLowerCase().indexOf(searchTerm) > -1 || i.getSummary().toLowerCase().indexOf(searchTerm) > -1) {
				list.add(i);
			}
		}
		return list;
	}

	public void notifyListenersModelChanged() {
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
		JIRAIssue i = parent.getSelectedIssue();
		if (getIssues().contains(i)) {
			return i;
		}
		return null;
	}

	public void setIssue(JIRAIssue issue) {
		parent.setIssue(issue);
	}

	public void notifyListenersIssuesLoaded(int numberOfLoadedIssues) {
		for (JIRAIssueListModelListener l : listeners) {
			l.issuesLoaded(this, numberOfLoadedIssues);
		}
	}

	public void modelChanged(JIRAIssueListModel model) {
		notifyListenersModelChanged();
	}

	public void issuesLoaded(JIRAIssueListModel model, int loadedIssues) {
		notifyListenersIssuesLoaded(loadedIssues);
	}
}
