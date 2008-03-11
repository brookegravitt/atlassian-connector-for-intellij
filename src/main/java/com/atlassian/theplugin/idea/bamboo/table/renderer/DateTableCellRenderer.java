package com.atlassian.theplugin.idea.bamboo.table.renderer;

import com.atlassian.theplugin.util.DateUtil;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateTableCellRenderer implements TableCellRenderer {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Component getTableCellRendererComponent(JTable jTable, Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
		JLabel label = new JLabel();

		label.setText(DateUtil.getRelativePastDate(new Date(), (Date) o));
		label.setToolTipText(dateFormat.format((Date) o));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel.add(label, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, null, null, null, 0, false));
		if (isSelected) {
			panel.setBackground(jTable.getSelectionBackground());
		} else {
			panel.setBackground(jTable.getBackground());
		}
		return panel;
	}
}
