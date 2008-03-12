package com.atlassian.theplugin.idea.bamboo.table.renderer;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


public class BuildNumberCellRenderer implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable jTable,
												   Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
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
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel.add(label, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_EAST,
				GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
				GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
				null, null, null, 0, false));

		if (isSelected) {
			panel.setBackground(jTable.getSelectionBackground());
		} else {
			panel.setBackground(jTable.getBackground());
		}		
		return panel;
	}
}