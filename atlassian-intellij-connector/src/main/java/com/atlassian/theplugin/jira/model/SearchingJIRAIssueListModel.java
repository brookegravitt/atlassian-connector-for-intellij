package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchingJIRAIssueListModel extends JIRAIssueListModelListenerHolder {
	private String searchTerm;

	public SearchingJIRAIssueListModel(JIRAIssueListModel parent) {
		super(parent);
		searchTerm = "";
	}

	public void setSearchTerm(@NotNull String searchTerm) {

		if (this.searchTerm.equals(searchTerm)) {
			return;
		}
		this.searchTerm = searchTerm.toLowerCase();
		fireModelChanged();
	}

	public Collection<JIRAIssue> search(Collection<JIRAIssue> col) {
		if (searchTerm.length() == 0) {
			return col;
		}
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();
		for (JIRAIssue i : col) {
			if (i.getKey().toLowerCase().indexOf(searchTerm) > -1
					|| i.getSummary().toLowerCase().indexOf(searchTerm) > -1) {
				list.add(i);
			}
		}
		return list;
	}

	public Collection<JIRAIssue> getIssues() {
		return search(parent.getIssues());
	}

	public Collection<JIRAIssue> getIssuesNoSubtasks() {
		return search(parent.getIssuesNoSubtasks());
	}

	public Collection<JIRAIssue> getSubtasks(JIRAIssue p) {
		return search(parent.getSubtasks(p));
	}
}
