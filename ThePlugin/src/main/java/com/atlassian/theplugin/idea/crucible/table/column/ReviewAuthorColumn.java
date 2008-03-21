package com.atlassian.theplugin.idea.crucible.table.column;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewAdapter;

import java.util.Comparator;


public class ReviewAuthorColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 120;

	public String getColumnName() {
		return "Author";
	}

	public Object valueOf(Object o) {
         return ((CrucibleReviewAdapter) o).getAuthor();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((CrucibleReviewAdapter) o).getAuthor().compareTo(((CrucibleReviewAdapter) o1).getAuthor());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}