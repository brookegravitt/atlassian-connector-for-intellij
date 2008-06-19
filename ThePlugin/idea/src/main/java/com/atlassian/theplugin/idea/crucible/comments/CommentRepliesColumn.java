package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 1:32:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentRepliesColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 50;

	public String getColumnName() {
		return "Replies";
	}

	public Object valueOf(Object o) {
		return String.valueOf(((GeneralComment) o).getReplies().size());
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return Integer.valueOf(((GeneralComment) o).getReplies().size()).compareTo(
						((GeneralComment) o1).getReplies().size()
				);
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}

}
