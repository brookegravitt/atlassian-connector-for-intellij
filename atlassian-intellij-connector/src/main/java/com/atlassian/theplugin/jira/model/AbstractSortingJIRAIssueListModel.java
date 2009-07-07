package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractSortingJIRAIssueListModel extends JIRAIssueListModelListenerHolder {

	public AbstractSortingJIRAIssueListModel(JIRAIssueListModel parent) {
		super(parent);
	}

	protected abstract Comparator<JIRAIssue> getComparator();

	private Collection<JIRAIssue> sort(Collection<JIRAIssue> col) {
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();
		for (JIRAIssue i : col) {
			list.add(i);
		}
		Collections.sort(list, getComparator());
		return list;
	}

	public Collection<JIRAIssue> getIssues() {
		return sort(parent.getIssues());
	}

	public Collection<JIRAIssue> getIssuesNoSubtasks() {
		return sort(parent.getIssuesNoSubtasks());
	}

	@NotNull
	public Collection<JIRAIssue> getSubtasks(JIRAIssue p) {
		return sort(parent.getSubtasks(p));
	}

	public JIRAIssue findIssue(String key) {
		return parent.findIssue(key);
	}
}
