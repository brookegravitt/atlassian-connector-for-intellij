package com.atlassian.theplugin.idea.bamboo.table.renderer;

import com.atlassian.theplugin.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateTableCellRenderer extends DefaultTableCellRenderer {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Component getTableCellRendererComponent(JTable jTable,
												   Object o,
												   boolean isSelected,
												   boolean hasFocus,
												   int i,
												   int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o != null && o instanceof Date) {
			((JLabel) c).setToolTipText(dateFormat.format((Date) o));
			((JLabel) c).setText(DateUtil.getRelativePastDate(new Date(), (Date) o));
		} else {
			((JLabel) c).setToolTipText("Build date not accessible");
			((JLabel) c).setText("-");
		}
		((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
		return c; 
	}
}
