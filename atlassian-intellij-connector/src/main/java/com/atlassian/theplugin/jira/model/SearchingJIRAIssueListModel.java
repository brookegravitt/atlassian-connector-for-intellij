package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchingJIRAIssueListModel extends JIRAIssueListModelListenerHolder implements JIRAIssueListModel {
	private final JIRAIssueListModel parent;

	private String searchTerm;

	public SearchingJIRAIssueListModel(JIRAIssueListModel parent) {
		this.parent = parent;
		parent.addModelListener(this);
		searchTerm = "";
	}

	public void setSearchTerm(@NotNull String searchTerm) {

		if (this.searchTerm.equals(searchTerm)) {
			return;
		}
		this.searchTerm = searchTerm.toLowerCase();
		fireModelChanged();
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

	public void addModelListener(JIRAIssueListModelListener listener) {
		addListener(listener);
	}

	public void removeModelListener(JIRAIssueListModelListener listener) {
		removeListener(listener);
	}

	public boolean isModelFrozen() {
		return parent.isModelFrozen();
	}

	public void setModelFrozen(boolean frozen) {
		parent.setModelFrozen(frozen);
	}

	public void addFrozenModelListener(FrozenModelListener listener) {
		parent.addFrozenModelListener(listener);
	}

	public void removeFrozenModelListener(FrozenModelListener listener) {
		parent.removeFrozenModelListener(listener);
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

	public void fireIssuesLoaded(int numberOfLoadedIssues) {
		issuesLoaded(this, numberOfLoadedIssues);
	}

	public void fireModelChanged() {
		modelChanged(this);
	}
}
