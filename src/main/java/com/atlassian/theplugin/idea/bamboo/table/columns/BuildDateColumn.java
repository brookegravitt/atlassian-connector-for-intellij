package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:24:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildDateColumn extends BambooColumnInfo {
	private static final int COL_WIDTH = 100;

	private SimpleDateFormat buildTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String getColumnName() {
		return "Build date";
	}

	public Object valueOf(Object o) {
		// @todo mwent - find renderer
        return buildTimeFormat.format(((BambooBuildAdapter) o).getBuildTime());
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((BambooBuildAdapter) o).getBuildTime().compareTo(((BambooBuildAdapter) o1).getBuildTime());
			}
		};
	}

	public int getWidth(JTable jTable) {
		return COL_WIDTH;
	}
}