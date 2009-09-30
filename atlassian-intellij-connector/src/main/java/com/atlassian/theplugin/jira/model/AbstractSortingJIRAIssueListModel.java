package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractSortingJIRAIssueListModel extends JIRAIssueListModelListenerHolder {

	public AbstractSortingJIRAIssueListModel(JIRAIssueListModel parent) {
		super(parent);
	}

	protected abstract Comparator<JiraIssueAdapter> getComparator();

	private Collection<JiraIssueAdapter> sort(Collection<JiraIssueAdapter> col) {
		List<JiraIssueAdapter> list = new ArrayList<JiraIssueAdapter>();
		for (JiraIssueAdapter i : col) {
			list.add(i);
		}
		Collections.sort(list, getComparator());
		return list;
	}

	public Collection<JiraIssueAdapter> getIssues() {
		return sort(parent.getIssues());
	}

	public Collection<JiraIssueAdapter> getIssuesNoSubtasks() {
		return sort(parent.getIssuesNoSubtasks());
	}

	@NotNull
	public Collection<JiraIssueAdapter> getSubtasks(JiraIssueAdapter p) {
		return sort(parent.getSubtasks(p));
	}

	public JiraIssueAdapter findIssue(String key) {
		return parent.findIssue(key);
	}
}
