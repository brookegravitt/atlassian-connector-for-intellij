package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 17, 2008
 * Time: 2:24:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentSummaryColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 200;

	public String getColumnName() {
		return "Comment";
	}

	public Object valueOf(Object o) {
         return "";
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((String) o).compareTo(((String) o1));
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
