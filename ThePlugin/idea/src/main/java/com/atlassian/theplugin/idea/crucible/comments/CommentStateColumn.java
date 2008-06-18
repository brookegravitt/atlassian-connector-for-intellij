package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

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
         return ((GeneralComment) o).getState().toString();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((GeneralComment) o).getState()
						.compareTo(((GeneralComment) o1).getState());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
