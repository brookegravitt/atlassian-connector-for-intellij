package com.atlassian.theplugin.idea.jira.table.columns;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.jira.IssueKeyComparator;

import java.util.Comparator;

public class IssueKeyColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 50;

	public Object valueOf(Object o) {
		return ((JiraIssueAdapter) o).getKey();
	}

	public String getColumnName() {
		return "Key";
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new IssueKeyComparator();
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}