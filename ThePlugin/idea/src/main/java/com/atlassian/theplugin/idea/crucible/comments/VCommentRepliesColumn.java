package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 20, 2008
 * Time: 2:35:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class VCommentRepliesColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 50;

	public String getColumnName() {
		return "Replies";
	}

	public Object valueOf(Object o) {
		return String.valueOf(((VersionedComment) o).getReplies().size());
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return Integer.valueOf(((VersionedComment) o).getReplies().size()).compareTo(
						((VersionedComment) o1).getReplies().size()
				);
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
