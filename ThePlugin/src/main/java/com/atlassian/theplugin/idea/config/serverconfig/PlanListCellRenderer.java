package com.atlassian.theplugin.idea.config.serverconfig;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlanListCellRenderer implements ListCellRenderer {
	protected static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

	private static final Icon FAVOURITE_ON_ICON = IconLoader.getIcon("/icons/fav_on.gif");
	private static final Icon FAVOURITE_OFF_ICON = IconLoader.getIcon("/icons/fav_off.gif");
	private static final Icon DISABLED_ICON = IconLoader.getIcon("/icons/icn_plan_disabled-16.gif");

	public Component getListCellRendererComponent(JList list, Object value, int index,
												  boolean isSelected, boolean cellHasFocus) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JLabel label = new JLabel();
		panel.add(label);

		if (value instanceof BambooPlanItem) {
			BambooPlanItem pi = (BambooPlanItem) value;
			JCheckBox checkBox = new JCheckBox(pi.getPlan().getPlanKey());
			label.setIcon(pi.getPlan().isEnabled()
					? (pi.getPlan().isFavourite() ? FAVOURITE_ON_ICON : FAVOURITE_OFF_ICON) 
					: DISABLED_ICON);
			checkBox.setText(pi.getPlan().getPlanKey());
			checkBox.setSelected(pi.isSelected());
			panel.add(checkBox);

			checkBox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			checkBox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

			checkBox.setEnabled(list.isEnabled());
			checkBox.setFont(list.getFont());
			checkBox.setFocusPainted(false);
			checkBox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : NO_FOCUS_BORDER);
		} else {
			label.setText(value.toString());
		}
		panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
		panel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
		panel.setEnabled(list.isEnabled());

		return panel;
	}
}



