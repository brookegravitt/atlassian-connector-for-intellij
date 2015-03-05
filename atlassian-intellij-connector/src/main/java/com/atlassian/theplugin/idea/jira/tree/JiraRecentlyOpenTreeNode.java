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
package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

/**
 * @author Jacek Jaroczynski
 */
public class JiraRecentlyOpenTreeNode extends AbstractTreeNode {

	private static final String NODE_NAME = "Recently Viewed Issues";

	public JiraRecentlyOpenTreeNode() {
		super(NODE_NAME, null, null);
	}

	@Override
	public String toString() {
		return NODE_NAME;
	}

	public JComponent getRenderer(final JComponent c, final boolean selected, final boolean expanded, final boolean hasFocus) {
//		Color bgColor = UIUtil.getTreeTextBackground();
//		Color fgColor = UIUtil.getTreeTextForeground();
//
//		JLabel label = new JLabel(NODE_NAME, JIRA_SERVER_ENABLED_ICON, SwingUtilities.LEADING);
//		label.setForeground(fgColor);
//		label.setBackground(bgColor);
//		label.setDisabledIcon(JIRA_SERVER_DISABLED_ICON);
//
//		label.setEnabled(c.isEnabled());
//		label.setOpaque(true);
//		return label;
//
		return new JLabel("Incorrect renderer");
	}
}
