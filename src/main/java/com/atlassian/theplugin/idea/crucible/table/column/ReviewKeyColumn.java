package com.atlassian.theplugin.idea.crucible.table.column;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import java.util.Comparator;


public class ReviewKeyColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 120;

	public String getColumnName() {
		return "Key";
	}

	public Object valueOf(Object o) {
         return ((ReviewDataInfoAdapter) o).getPermaId().getId();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((ReviewDataInfoAdapter) o).getPermaId().getId()
						.compareTo(((ReviewDataInfoAdapter) o1).getPermaId().getId());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}