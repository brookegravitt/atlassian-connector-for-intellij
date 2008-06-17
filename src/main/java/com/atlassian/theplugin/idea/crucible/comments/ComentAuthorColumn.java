package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 17, 2008
 * Time: 2:27:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ComentAuthorColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 120;

	public String getColumnName() {
		return "Author";
	}

	public Object valueOf(Object o) {
         return ((ReviewDataInfoAdapter) o).getAuthor();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((ReviewDataInfoAdapter) o).getAuthor().compareTo(((ReviewDataInfoAdapter) o1).getAuthor());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
