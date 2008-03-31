package com.atlassian.theplugin.idea.jira.table;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.jira.table.columns.IssueKeyColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueStatusColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueSummaryColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueTypeColumn;
import com.atlassian.theplugin.idea.jira.table.renderers.IssueStatusCellRenderer;
import com.atlassian.theplugin.idea.jira.table.renderers.IssueTypeCellRenderer;

import javax.swing.table.TableCellRenderer;

public final class JIRATableColumnProvider {
	private JIRATableColumnProvider() {
	}

	public static TableColumnInfo[] makeColumnInfo() {
		return new TableColumnInfo[]{
				new IssueTypeColumn(),
				new IssueStatusColumn(),
				new IssueKeyColumn(),
				new IssueSummaryColumn()
		};
	}

	public static TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				new IssueTypeCellRenderer(),
				new IssueStatusCellRenderer(),
				null,
				null
		};
	}
}