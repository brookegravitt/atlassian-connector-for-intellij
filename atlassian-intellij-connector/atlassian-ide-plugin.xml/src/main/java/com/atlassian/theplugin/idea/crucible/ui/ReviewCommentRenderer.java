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
package com.atlassian.theplugin.idea.crucible.ui;

import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.util.IdeaIconProvider;
import com.atlassian.theplugin.util.ui.IconProvider;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class ReviewCommentRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

	/**
	 * Useful for injecting your own IconProvider. Facilitates testing outside IDEA framework
	 *
	 * @param iconProvider provider used for retrieving icons
	 */
	public ReviewCommentRenderer(final IconProvider iconProvider) {
		reviewCommentPanel = new ReviewCommentPanel(iconProvider);
	}

	/**
	 * uses default IDEA-specific icon provider. Not testable outside IDEA
	 */
	public ReviewCommentRenderer() {
		this(new IdeaIconProvider());
	}

	private final ReviewCommentPanel reviewCommentPanel;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, final boolean isSelected, boolean expanded,
			boolean leaf, int row, boolean aHasFocus) {
		if (value instanceof CommentTreeNode) {
			final CommentTreeNode node = (CommentTreeNode) value;
			reviewCommentPanel.update(node.getReview(), node.getComment(), getAvailableWidth(node, tree),
					node.isExpanded(), isSelected, tree.getFont());
			return reviewCommentPanel;
		} else {
			return super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, aHasFocus);
		}
	}

	private int getAvailableWidth(DefaultMutableTreeNode obj, JTree jtree) {
		int i1 = jtree.getInsets().left + jtree.getInsets().right + getNesting(jtree) * (obj.getLevel() + 1);
		return jtree.getVisibleRect().width - i1 - 2;
	}

	private int getNesting(JTree jtree) {
		TreeUI treeui = jtree.getUI();
		if (treeui instanceof BasicTreeUI) {
			BasicTreeUI basictreeui = (BasicTreeUI) treeui;
			return basictreeui.getLeftChildIndent() + basictreeui.getRightChildIndent();
		} else {
			return (Integer) UIUtil.getTreeLeftChildIndent() + (Integer) UIUtil.getTreeRightChildIndent();
		}
	}

}

