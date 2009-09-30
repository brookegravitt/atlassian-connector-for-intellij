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

package com.atlassian.theplugin.idea.jira.renderers;

import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAQueryFragment;

import javax.swing.*;
import java.awt.*;

public class JIRAQueryFragmentListRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(
			JList jList, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel comp = (JLabel) super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
		if (comp != null && value != null && value instanceof JIRAQueryFragment) {
			comp.setText(((JIRAQueryFragment) value).getName());
		}
		return comp;
	}
}