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

import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.commons.bamboo.BambooBuildAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Date;


public class DateTableCellRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable jTable,
												   Object o,
												   boolean isSelected,
												   boolean hasFocus,
												   int i,
												   int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o != null && o instanceof Date) {
			((JLabel) c).setToolTipText(BambooBuildAdapter.BAMBOO_BUILD_DATE_FORMAT.format((Date) o));
			((JLabel) c).setText(DateUtil.getRelativePastDate(new Date(), (Date) o));
		} else {
			((JLabel) c).setToolTipText("Build date not accessible");
			((JLabel) c).setText("-");
		}
		((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
		return c; 
	}
}
