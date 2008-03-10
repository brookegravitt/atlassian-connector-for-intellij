package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;

import javax.swing.*;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:24:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildNumberColumn extends BambooColumnInfo {
	private static final int COL_WIDTH = 100;

	public String getColumnName() {
		return "Build number";
	}

	public Object valueOf(Object o) {
		return Integer.valueOf(((BambooBuildAdapter) o).getBuildNumber());
	}

	public Class getColumnClass() {
		return Integer.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
			    int oValue = Integer.parseInt(((BambooBuildAdapter) o).getBuildNumber());
				int o1Value = Integer.parseInt(((BambooBuildAdapter) o1).getBuildNumber());
				return oValue - o1Value;
			}
		};
	}

	public int getWidth(JTable jTable) {
		return COL_WIDTH;
	}
}