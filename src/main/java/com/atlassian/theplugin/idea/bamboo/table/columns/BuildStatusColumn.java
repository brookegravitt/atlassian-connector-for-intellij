package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.TableColumnInfo;

import javax.swing.*;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:24:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildStatusColumn extends TableColumnInfo {
	private static final int COL_ICON_WIDTH = 20;

	public String getColumnName() {
		return "";
	}

	public Object valueOf(Object o) {
		return ((BambooBuildAdapter) o).getBuildIcon();
	}

	public Class getColumnClass() {
		return Icon.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((BambooBuildAdapter) o).getStatus().compareTo(((BambooBuildAdapter) o1).getStatus());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_ICON_WIDTH;
	}

}
