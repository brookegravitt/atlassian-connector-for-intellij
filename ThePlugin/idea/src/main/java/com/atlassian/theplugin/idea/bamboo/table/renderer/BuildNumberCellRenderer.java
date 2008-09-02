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

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;


public class BuildNumberCellRenderer extends BackgroundAwareBambooRenderer {

	protected void onRender(ListTableModel<BambooBuildAdapterIdea> model, JLabel label, Object o) {
		if (o instanceof Integer) {
			int value = (Integer) o;
			if (value == 0) {
				label.setToolTipText("Build information not accessible");
				label.setText("-");
			} else {
				label.setToolTipText(o.toString());
				label.setText(o.toString());
			}
			label.setHorizontalAlignment(SwingConstants.RIGHT);
		}
	}

}