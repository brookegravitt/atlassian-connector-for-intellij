package com.atlassian.theplugin.idea.crucible.table.column;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewAdapter;

import java.util.Comparator;


public class ReviewSummaryColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 200;

	public String getColumnName() {
		return "Summary";
	}

	public Object valueOf(Object o) {
         return ((CrucibleReviewAdapter) o).getDescription();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((CrucibleReviewAdapter) o).getDescription().compareTo(((CrucibleReviewAdapter) o1).getDescription());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}