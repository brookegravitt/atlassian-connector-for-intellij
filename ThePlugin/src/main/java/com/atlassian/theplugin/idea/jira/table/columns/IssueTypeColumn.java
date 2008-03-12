package com.atlassian.theplugin.idea.jira.table.columns;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.jira.api.JIRAIssue;

import javax.swing.*;
import java.util.Comparator;

public class IssueTypeColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 20;

	public Object valueOf(Object o) {
		return new ImageIcon(((JIRAIssue) o).getTypeIconUrl());
	}

	public Class getColumnClass() {
		return Icon.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((JIRAIssue) o).getType().compareTo(((JIRAIssue) o1).getType());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}

	public String getColumnName() {
		return "";
	}
}
