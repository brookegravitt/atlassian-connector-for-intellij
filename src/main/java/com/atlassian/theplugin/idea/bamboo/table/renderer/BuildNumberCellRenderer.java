package com.atlassian.theplugin.idea.bamboo.table.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;


public class BuildNumberCellRenderer implements TableCellRenderer {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
		JLabel label = new JLabel();
		if (o instanceof Integer) {
			int value = (Integer) o;
			if (value == 0) {
				label.setText("-");
				label.setToolTipText("Build information not accessible");
			} else {
				label.setText(o.toString());							
			}

		}
		return label;
	}
}