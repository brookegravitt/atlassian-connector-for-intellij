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
package com.atlassian.theplugin.idea.bamboo.table.renderer;

import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BackgroundAwareBambooRenderer extends DefaultTableCellRenderer {
	private static final Color FAILED_BUILD_COLOR = new Color(255, 201, 201);
	private static final Color SUCCEED_BUILD_COLOR = new Color(203, 255, 165);
	private static final Color DISABLED_BUILD_COLOR = new Color(230, 230, 230);

	@Override
	public final Component getTableCellRendererComponent(JTable jTable,
			Object o, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, row, column);
		if (jTable.getModel() instanceof ListTableModel && c instanceof JLabel) {
			@SuppressWarnings("unchecked")
			ListTableModel<BambooBuildAdapterIdea> o1 = (ListTableModel<BambooBuildAdapterIdea>) jTable.getModel();
			BambooBuildAdapterIdea currentRow = (BambooBuildAdapterIdea) o1.getItem(row);
			if (isSelected == false) {
				if (currentRow.getEnabled()) {
					if (currentRow.getStatus() == BuildStatus.BUILD_FAILED) {
						c.setBackground(FAILED_BUILD_COLOR);
					} else {
						c.setBackground(SUCCEED_BUILD_COLOR);
					}
				} else {
					c.setBackground(DISABLED_BUILD_COLOR);
				}

			}

			onRender(o1, (JLabel) c, o);
		}
		return c;
	}

	protected void onRender(ListTableModel<BambooBuildAdapterIdea> model, JLabel label, Object o) {
	}
}
