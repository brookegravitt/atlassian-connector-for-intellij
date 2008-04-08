package com.atlassian.theplugin.idea.jira.table.columns;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

import java.util.Comparator;

public class IssuePriorityColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 20;
	public static final String COLUMN_NAME = "Priority";

	public Object valueOf(Object o) {
		return (JiraIssueAdapter) o;
	}

	public Class getColumnClass() {
		return JiraIssueAdapter.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((JiraIssueAdapter) o).getPriority().compareTo(((JiraIssueAdapter) o1).getPriority());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}

	public String getColumnName() {
		return COLUMN_NAME;
	}
}