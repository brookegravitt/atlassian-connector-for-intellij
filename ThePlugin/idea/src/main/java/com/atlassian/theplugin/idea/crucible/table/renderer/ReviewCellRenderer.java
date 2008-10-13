package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public abstract class ReviewCellRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable jTable,
												   Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o instanceof ReviewAdapter) {
			ReviewAdapter review = (ReviewAdapter) o;
			String tooltip = getCellToolTipText(review);
			if (tooltip != null) {
				((JLabel) c).setToolTipText(tooltip);
			}
			((JLabel) c).setIcon(null);
			((JLabel) c).setText(new ReviewDecoratorImpl(getCellText(review), review, isSelected).getString());
		}
		return c;
	}

	protected abstract String getCellText(ReviewAdapter review);
	protected abstract String getCellToolTipText(ReviewAdapter review);
}
