package com.atlassian.theplugin.idea.bamboo.table.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateTableCellRenderer implements TableCellRenderer {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
		JLabel label = new JLabel();
		if (o instanceof Date) {
			label.setText(dateFormat.format((Date) o));
			label.setToolTipText(label.getText());

		}
		return label;
	}
}
