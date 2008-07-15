package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.TableColumnInfo;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.util.Comparator;
import java.awt.*;

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

	@Override
	public TableCellRenderer getCustomizedRenderer(Object o, TableCellRenderer tableCellRenderer) {
		return new MultiLineRenderer();
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

	private class MultiLineRenderer extends JTextArea implements TableCellRenderer {
		public MultiLineRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
		}

		public Component getTableCellRendererComponent(JTable jTable, Object value, boolean b, boolean b1, int i, int i1) {
			setText((String) value);
			return this;
		}
	}
}
