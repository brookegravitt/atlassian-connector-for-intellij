package com.atlassian.theplugin.idea.crucible.table.column;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import java.util.Comparator;
import java.util.Iterator;


public class ReviewReviewersColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 200;

	public String getColumnName() {
		return "Reviewers";
	}

	public Object valueOf(Object o) {
		String reviewers = "<html>";
		reviewers += getReviewersAsText(o);
		reviewers += "</html>";
		return reviewers;				
	}

	private String getReviewersAsText(Object o) {
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iterator = ((ReviewDataInfoAdapter) o).getReviewers().iterator(); iterator.hasNext();) {
			sb.append(iterator.next());
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				String r = getReviewersAsText(o);
				String r1 = getReviewersAsText(o1);
				return r.compareTo(r1);
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}


}