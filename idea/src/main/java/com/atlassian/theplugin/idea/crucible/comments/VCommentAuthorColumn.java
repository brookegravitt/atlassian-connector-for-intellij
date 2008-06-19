package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 7:57:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class VCommentAuthorColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 120;

	public String getColumnName() {
		return "Author";
	}

	public Object valueOf(Object o) {
         return ((VersionedComment) o).getDisplayUser();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((VersionedComment) o).getDisplayUser().compareTo(((VersionedComment) o1).getDisplayUser());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
