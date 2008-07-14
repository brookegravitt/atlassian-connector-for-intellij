package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 7:56:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class VCommentCreateDateColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 120;
	public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("d MMM yyyy, HH:mm Z");

	public String getColumnName() {
		return "Created";
	}

	public Object valueOf(Object o) {
		return FORMATTER.format(((VersionedComment) o).getCreateDate());
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((VersionedComment) o).getCreateDate().compareTo(((VersionedComment) o1).getCreateDate());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}

}
