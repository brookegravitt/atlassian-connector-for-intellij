package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.TableColumnInfo;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 7:58:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class VCommentSummaryColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 200;

	public String getColumnName() {
		return "Comment";
	}

	public Object valueOf(Object o) {
         return ((VersionedComment) o).getMessage();
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((VersionedComment) o).getMessage().compareTo(((VersionedComment) o1).getMessage());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
