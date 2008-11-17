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

import com.atlassian.theplugin.commons.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import java.util.Date;


public class DateTableCellRenderer extends BackgroundAwareBambooRenderer {

	@Override
	protected void onRender(final ListTableModel<BambooBuildAdapterIdea> model, final JLabel label, final Object o) {
		if (o != null && o instanceof Date) {
			label.setToolTipText(BambooBuildAdapter.BAMBOO_BUILD_DATE_FORMAT.format((Date) o));
			label.setText(DateUtil.getRelativePastDate(new Date(), (Date) o));
		} else {
			label.setToolTipText("Build date not accessible");
			label.setText("-");
		}
		label.setHorizontalAlignment(SwingConstants.RIGHT);
	}
}
