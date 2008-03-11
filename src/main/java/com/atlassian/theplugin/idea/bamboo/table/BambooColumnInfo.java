package com.atlassian.theplugin.idea.bamboo.table;

import com.intellij.util.ui.ColumnInfo;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:29:52 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BambooColumnInfo extends ColumnInfo {
	private BambooColumnInfo(String s) {
		super(s);
	}

	public BambooColumnInfo() {
		this("");
		setName(getColumnName());
	}

	public abstract String getColumnName();

	public abstract Object valueOf(Object o);

	public abstract Class getColumnClass();

	public abstract Comparator getComparator();

	public abstract int getPrefferedWidth();
}
