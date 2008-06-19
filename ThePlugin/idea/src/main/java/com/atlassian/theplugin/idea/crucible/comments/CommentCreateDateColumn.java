package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

import java.util.Comparator;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 10:08:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentCreateDateColumn extends TableColumnInfo {
	private static final int COL_WIDTH = 120;
	public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("d MMM yyyy, HH:mm Z");

	public String getColumnName() {
		return "Created";
	}

	public Object valueOf(Object o) {
		return FORMATTER.format(((GeneralComment) o).getCreateDate());
	}

	public Class getColumnClass() {
		return String.class;
	}

	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o, Object o1) {
				return ((GeneralComment) o).getCreateDate().compareTo(((GeneralComment) o1).getCreateDate());
			}
		};
	}

	public int getPrefferedWidth() {
		return COL_WIDTH;
	}
}
