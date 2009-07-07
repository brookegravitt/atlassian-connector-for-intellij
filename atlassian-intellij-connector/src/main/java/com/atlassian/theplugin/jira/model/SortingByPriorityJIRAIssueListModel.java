package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.commons.jira.api.JIRAIssue;

import java.util.Comparator;
import java.util.Set;

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

	public void setActiveJiraIssue(final ActiveJiraIssueBean issue) {
	}

	public ActiveJiraIssueBean getActiveJiraIssue() {
		return null;
	}

	public void clearCache() {
	}

	public Set<JIRAIssue> getIssuesCache() {
		return null;
	}

	public JIRAIssue getIssueFromCache(final IssueRecentlyOpenBean recentIssue) {
		return null;
	}
}
