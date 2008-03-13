package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.TableColumnInfo;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 10, 2008
 * Time: 12:24:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildErrorMessageColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 300;

	public String getColumnName() {
		return "Message";
	}

	public Object valueOf(Object o) {
		return ((BambooBuildAdapter) o).getMessage();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((BambooBuildAdapter) o).getMessage()
						.compareTo(((BambooBuildAdapter) o1).getMessage());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}