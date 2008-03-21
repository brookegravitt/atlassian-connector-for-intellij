package com.atlassian.theplugin.idea.crucible.table.column;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewAdapter;

import java.util.Comparator;


public class ReviewStateColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 200;

	public String getColumnName() {
		return "State";
	}

	public Object valueOf(Object o) {
         return ((CrucibleReviewAdapter) o).getState().value();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((CrucibleReviewAdapter) o).getState().value().compareTo(((CrucibleReviewAdapter) o1).getState().value());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}