package com.atlassian.theplugin.idea.jira.table;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.jira.table.columns.*;
import com.atlassian.theplugin.idea.jira.table.renderers.IssueStatusCellRenderer;
import com.atlassian.theplugin.idea.jira.table.renderers.IssueTypeCellRenderer;
import com.atlassian.theplugin.idea.jira.table.renderers.IssuePriorityCellRenderer;

import javax.swing.table.TableCellRenderer;

public final class JIRATableColumnProvider {
	private JIRATableColumnProvider() {
	}

	public static TableColumnInfo[] makeColumnInfo() {
		return new TableColumnInfo[]{
				new IssueTypeColumn(),
				new IssueStatusColumn(),
				new IssuePriorityColumn(),
				new IssueKeyColumn(),
				new IssueSummaryColumn()
		};
	}

	public static TableCellRenderer[] makeRendererInfo() {
		return new TableCellRenderer[]{
				new IssueTypeCellRenderer(),
				new IssueStatusCellRenderer(),
				new IssuePriorityCellRenderer(),
				null,
				null
		};
	}
}