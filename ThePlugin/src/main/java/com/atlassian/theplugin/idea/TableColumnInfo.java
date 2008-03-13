package com.atlassian.theplugin.idea;

import com.intellij.util.ui.ColumnInfo;

import java.util.Comparator;


public abstract class TableColumnInfo extends ColumnInfo {
	private TableColumnInfo(String s) {
		super(s);
	}

	public TableColumnInfo() {
		this("");
		setName(getColumnName());
	}

	public abstract String getColumnName();

	public abstract Object valueOf(Object o);

	public abstract Class getColumnClass();

	public abstract Comparator getComparator();

	public abstract int getPrefferedWidth();
}
