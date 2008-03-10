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
public class BuildServerColumn extends BambooColumnInfo {
	private static final int COL_WIDTH = 100;

	public String getColumnName() {		
		return "Server";
	}

	public Object valueOf(Object o) {
		return ((BambooBuildAdapter) o).getServer().getName();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((BambooBuildAdapter) o).getServer().getName()
						.compareTo(((BambooBuildAdapter) o1).getServer().getName());
			}
		};
	}

	public int getWidth(JTable jTable) {
		return COL_WIDTH;
	}
}