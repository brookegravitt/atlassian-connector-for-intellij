/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.config.serverconfig;

import com.intellij.openapi.util.IconLoader;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlanListCellRenderer implements ListCellRenderer {
	protected static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

	private static final Icon FAVOURITE_ON_ICON = IconLoader.getIcon("/icons/fav_on.gif");
	private static final Icon FAVOURITE_OFF_ICON = IconLoader.getIcon("/icons/fav_off.gif");
	private static final Icon DISABLED_ICON = IconLoader.getIcon("/icons/icn_plan_disabled-16.gif");
    public static final String GROUP_NAME = "group";

    public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		JPanel panel = new JPanel();
        CellConstraints cc = new CellConstraints();
		panel.setLayout(new FormLayout("pref, 5dlu, pref, pref:grow, pref, 5dlu", "pref"));
		JLabel label = new JLabel();
		panel.add(label, cc.xy(1, 1));
        Color background =
                isSelected ? list.getSelectionBackground() : index % 2 == 0 ? new Color(238, 229, 222)
                        : list.getBackground();

		if (value instanceof BambooPlanItem) {

			BambooPlanItem pi = (BambooPlanItem) value;
			JCheckBox checkBox = new JCheckBox(pi.getPlan().getKey());
            JCheckBox groupedBox = new JCheckBox();

            groupedBox.setSelected(pi.isGrouped());
            groupedBox.setBackground(background);
            groupedBox.setName(GROUP_NAME);

			label.setIcon(pi.getPlan().isEnabled()
					? (pi.getPlan().isFavourite() ? FAVOURITE_ON_ICON : FAVOURITE_OFF_ICON)
					: DISABLED_ICON);
            label.setBackground(background);
			checkBox.setText(pi.getPlan().getKey());
			checkBox.setSelected(pi.isSelected());
            checkBox.setBackground(background);

			panel.add(checkBox, cc.xy(3, 1));

			checkBox.setBackground(background);
			checkBox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

			checkBox.setEnabled(list.isEnabled());
			checkBox.setFont(list.getFont());
			checkBox.setFocusPainted(false);
			checkBox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : NO_FOCUS_BORDER);

            final JPanel growPanel = new JPanel();
            growPanel.setBackground(background);
            growPanel.setPreferredSize(checkBox.getPreferredSize());
            panel.add(growPanel, cc.xy(4, 1));
            panel.add(groupedBox, cc.xy(5, 1));
            
            groupedBox.setEnabled(list.isEnabled());
            groupedBox.setBackground(background);
            groupedBox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            groupedBox.setFocusPainted(false);
            groupedBox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : NO_FOCUS_BORDER);
            groupedBox.setToolTipText("Group builds");

            final JPanel finalPanel = new JPanel();
            finalPanel.setBackground(background);
            finalPanel.setPreferredSize(checkBox.getPreferredSize());
            panel.add(finalPanel, cc.xy(6, 1));

		} else {
			label.setText(value.toString());
            label.setBackground(background);
		}
		panel.setBackground(list.getBackground());
		panel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
		panel.setEnabled(list.isEnabled());

		return panel;
	}
}



