package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;

import java.util.Comparator;

public class BuildKeyColumn extends BambooColumnInfo {
	private static final int COL_BUILD_KEY_WIDTH = 100;

	public String getColumnName() {
		return "Build plan";
	}

	public Object valueOf(Object o) {
		return ((BambooBuildAdapter) o).getBuildKey();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((BambooBuildAdapter) o).getBuildKey().compareTo(((BambooBuildAdapter) o1).getBuildKey());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_BUILD_KEY_WIDTH;
	}
}