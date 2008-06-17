package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 17, 2008
 * Time: 2:28:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentStateColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 200;

	public String getColumnName() {
		return "State";
	}

	public Object valueOf(Object o) {
         return ((ReviewDataInfoAdapter) o).getState().value();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((ReviewDataInfoAdapter) o).getState().value()
						.compareTo(((ReviewDataInfoAdapter) o1).getState().value());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
