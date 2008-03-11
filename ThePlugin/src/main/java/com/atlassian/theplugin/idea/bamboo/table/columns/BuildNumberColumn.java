package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;

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
		String number = ((BambooBuildAdapter) o).getBuildNumber();
		if (number != null) {
			return Integer.valueOf(number);
		} else {
			return Integer.valueOf(0);
		}

	}

	public Class getColumnClass() {
		return Integer.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				String oStr = (((BambooBuildAdapter) o).getBuildNumber());
				String o1Str = ((BambooBuildAdapter) o1).getBuildNumber();
				if (oStr != null && o1Str != null) {
					int oValue = Integer.parseInt(oStr);
					int o1Value = Integer.parseInt(o1Str);
					return oValue - o1Value;
				}
				return 0;
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}