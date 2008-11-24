package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.Comparator;

public class SortingByPriorityJIRAIssueListModel extends AbstractSortingJIRAIssueListModel {
	static final Comparator<JIRAIssue> PRIORITY_ORDER = new Comparator<JIRAIssue>() {
		public int compare(JIRAIssue i1, JIRAIssue i2) {
			return (int) (i1.getPriorityId() - i2.getPriorityId());
		}
	};

	public SortingByPriorityJIRAIssueListModel(JIRAIssueListModel parent) {
		super(parent);
	}

	protected Comparator<JIRAIssue> getComparator() {
		return PRIORITY_ORDER;
	}
}
