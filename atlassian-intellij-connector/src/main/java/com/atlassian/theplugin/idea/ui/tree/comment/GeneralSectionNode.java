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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.util.PluginUtil;
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
	private static final String GENERAL_COMMENTS_SECTION = "General Comments";

	private TreeCellRenderer myRenderer;
	private ReviewAdapter review;


	public GeneralSectionNode(ReviewAdapter review, AtlassianClickAction action) {
		super(action);
		this.review = review;
		initRenderer();

	}

	void initRenderer() {
			int noOfGeneralComments = 0;
		try {
			noOfGeneralComments = review.getGeneralComments().size();
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			PluginUtil.getLogger().error("No General Comments");
		}		
		this.myRenderer = new MyRenderer(noOfGeneralComments);
	}

	public GeneralSectionNode(GeneralSectionNode node) {
		super(node.getAtlassianClickAction());
		this.review = node.review;
		initRenderer();
	}

	@Override
    public TreeCellRenderer getTreeCellRenderer() {
		return myRenderer;
	}

	public ReviewAdapter getReview() {
		return review;
	}

	@Override
	public AtlassianTreeNode getClone() {
		return new GeneralSectionNode(this);
	}

	public int compareTo(Object o) {
		if (o instanceof GeneralSectionNode) {
			return 0;
		}
		return -1;
	}

	private class MyRenderer implements TreeCellRenderer {
		private int noOfGeneralComments = 0;

		MyRenderer(final int noOfGeneralComments) {
			this.noOfGeneralComments = noOfGeneralComments;
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			GeneralSectionNode node = (GeneralSectionNode) value;
			JPanel panel = new JPanel(new FormLayout("4dlu, left:pref:grow, 4dlu", "4dlu, pref:grow, 4dlu"));
			SimpleColoredComponent component = new SimpleColoredComponent();
			component.append(getGeneralCommentsSection(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			panel.add(component, new CellConstraints(2, 2));

			panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
					: BorderFactory.createEmptyBorder(1, 1, 1, 1));

			return panel;
		}

		private String getGeneralCommentsSection() {
			StringBuilder sb = new StringBuilder(GENERAL_COMMENTS_SECTION);
			sb.append(" (").append(noOfGeneralComments).append(")");
			return sb.toString();
		}
	}
}
