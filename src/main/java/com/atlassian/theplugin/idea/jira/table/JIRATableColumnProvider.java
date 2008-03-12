package com.atlassian.theplugin.idea.jira.table;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.jira.table.columns.IssueKeyColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueSummaryColumn;
import com.atlassian.theplugin.idea.jira.table.columns.IssueTypeColumn;

import javax.swing.table.TableCellRenderer;

public final class JIRATableColumnProvider {
	private JIRATableColumnProvider() {
	}

	public static TableColumnInfo[] makeColumnInfo() {
		return new TableColumnInfo[]{
				new IssueTypeColumn(),
				new IssueKeyColumn(),
				new IssueSummaryColumn()
		};
	}

	public static TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				null,
				null,
				null
		};
	}
}