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

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.util.CommentPanelBuilder;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class GeneralCommentTreeNode extends CommentTreeNode {
	private GeneralComment comment;
	private static final TreeCellRenderer MY_RENDERER = new MyRenderer();

	public GeneralCommentTreeNode(ReviewDataImpl review, GeneralComment comment, AtlassianClickAction action) {
		super(action);
		this.review = review;
		this.comment = comment;
	}

	public GeneralCommentTreeNode(final GeneralCommentTreeNode node) {
		super(node.getAtlassianClickAction());
		this.review = node.review;
		this.comment = node.comment;
	}

	public GeneralComment getComment() {
		return comment;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public AtlassianTreeNode getClone() {
		return new GeneralCommentTreeNode(this);
	}

	public int compareTo(Object o) {
		if (o instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode gctn = (GeneralCommentTreeNode) o;
			return getComment().getCreateDate().compareTo(gctn.getComment().getCreateDate());
		}
		return super.compareTo(o);
	}

	private static class MyRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) value;
			JPanel panel;
			if (node.isEditable()) {
				panel = CommentPanelBuilder.createEditPanelOfGeneralComment(
						node.getReview(), node.getComment());
			} else {
				panel = CommentPanelBuilder.createViewPanelOfGeneralComment(
						node.getReview(), node.getComment(), isSelected);
			}
			return panel;
		}
	}
}
