package com.atlassian.theplugin.idea.bamboo.table.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class BuildNumberCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable jTable,
												   Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o instanceof Integer) {
			int value = (Integer) o;
			if (value == 0) {
				((JLabel) c).setToolTipText("Build information not accessible");
				((JLabel) c).setText("-");
			} else {
				((JLabel) c).setToolTipText(o.toString());
				((JLabel) c).setText(o.toString());
			}
		}
		((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);				
		return c;
	}
}