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
public class ProjectKeyColumn extends BambooColumnInfo {
	private static final int COL_PROJECT_KEY_WIDTH = 100;

	public String getColumnName() {
		return "Project";
	}

	public Object valueOf(Object o) {
		return ((BambooBuildAdapter) o).getProjectName();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				if (((BambooBuildAdapter) o).getProjectName() != null
						&& ((BambooBuildAdapter) o1).getProjectName() != null) {
					return ((BambooBuildAdapter) o).getProjectName().
							compareTo(((BambooBuildAdapter) o1).getProjectName());
				} else {
					return 0;
				}
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_PROJECT_KEY_WIDTH;
	}
}