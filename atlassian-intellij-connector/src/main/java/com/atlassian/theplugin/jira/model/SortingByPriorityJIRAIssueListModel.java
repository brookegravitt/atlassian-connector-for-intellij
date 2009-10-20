package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;

import java.util.Comparator;
import java.util.Set;

public class SortingByPriorityJIRAIssueListModel extends AbstractSortingJIRAIssueListModel {
	static final Comparator<JiraIssueAdapter> PRIORITY_ORDER = new Comparator<JiraIssueAdapter>() {
		public int compare(JiraIssueAdapter i1, JiraIssueAdapter i2) {
			return (int) (i1.getPriorityId() - i2.getPriorityId());
		}
	};

	public SortingByPriorityJIRAIssueListModel(JIRAIssueListModel parent) {
		super(parent);
	}

	protected Comparator<JiraIssueAdapter> getComparator() {
		return PRIORITY_ORDER;
	}

	public void setActiveJiraIssue(final ActiveJiraIssueBean issue) {
	}

	public ActiveJiraIssueBean getActiveJiraIssue() {
		return null;
	}

	public void clearCache() {
	}

	public Set<JiraIssueAdapter> getIssuesCache() {
		return null;
	}

	public JiraIssueAdapter getIssueFromCache(final IssueRecentlyOpenBean recentIssue) {
		return null;
	}
}
