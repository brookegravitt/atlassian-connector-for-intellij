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

package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 21, 2008
 * Time: 3:57:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralSectionNode extends AtlassianTreeNode {
	private static final String GENERAL_COMMENTS_SECTION = "General comments";

	private static final TreeCellRenderer MY_RENDERER = new MyRenderer();
	private ReviewData review;

	public GeneralSectionNode(ReviewData review, AtlassianClickAction action) {
		super(action);
		this.review = review;
	}

	public GeneralSectionNode(GeneralSectionNode node) {
		super(node.getAtlassianClickAction());
		this.review = node.review;
	}

	@Override
    public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public ReviewData getReview() {
		return review;
	}

	public AtlassianTreeNode getClone() {
		return new GeneralSectionNode(this);
	}

	private static class MyRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			GeneralSectionNode node = (GeneralSectionNode) value;
			JPanel panel = new JPanel(new FormLayout("4dlu, left:pref:grow, 4dlu", "4dlu, pref:grow, 4dlu"));
			SimpleColoredComponent component = new SimpleColoredComponent();
			component.append(GENERAL_COMMENTS_SECTION, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			panel.add(component, new CellConstraints(2, 2));

			panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
					: BorderFactory.createEmptyBorder(1, 1, 1, 1));

			return panel;
		}
	}
}
