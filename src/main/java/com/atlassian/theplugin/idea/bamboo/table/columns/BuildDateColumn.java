package com.atlassian.theplugin.idea.bamboo.table.columns;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;
import com.atlassian.theplugin.util.DateUtil;

import java.util.Comparator;
import java.util.Date;


public class BuildDateColumn extends BambooColumnInfo {
	private static final int COL_WIDTH = 200;

	public String getColumnName() {
		return "Build date";
	}

	public Object valueOf(Object o) {
         return DateUtil.getRelativePastDate(new Date(), ((BambooBuildAdapter) o).getBuildTime());
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				if (((BambooBuildAdapter) o).getBuildTime() != null
						&& ((BambooBuildAdapter) o1).getBuildTime() != null) {
					return ((BambooBuildAdapter) o).getBuildTime()
							.compareTo(((BambooBuildAdapter) o1).getBuildTime());
				} else {
					return 0;
				}

			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}