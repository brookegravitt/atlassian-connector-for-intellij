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

import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;

import javax.swing.*;
import java.awt.*;

public class JIRAConstantListRenderer extends DefaultListCellRenderer {
	@Override
	public Component getListCellRendererComponent(JList jList, Object value, int i, boolean b, boolean b1) {
		JLabel comp = (JLabel) super.getListCellRendererComponent(jList, value, i, b, b1);
		if (comp != null && value != null && value instanceof JIRAConstant) {
			JIRAConstant c = (JIRAConstant) value;
			comp.setText(c.getName());
			Icon icon = CachedIconLoader.getIcon(c.getIconUrl());
			comp.setIcon(icon);
			if (c.getIconUrl() != null) {
				Icon disabledIcon = CachedIconLoader.getDisabledIcon(c.getIconUrl().toString());
				comp.setDisabledIcon(disabledIcon);
			} else {
				comp.setDisabledIcon(null);
			}
		}
		return comp;
	}
}
