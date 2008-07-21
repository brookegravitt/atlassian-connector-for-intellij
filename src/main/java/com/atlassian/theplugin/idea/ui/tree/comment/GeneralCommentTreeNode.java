package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.util.CommentPanelBuilder;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 11:01:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentTreeNode extends CommentTreeNode {
	private ReviewData review;
	private GeneralComment comment;
	private static final TreeCellRenderer MY_RENDERER = new MyRenderer();

	public GeneralCommentTreeNode(ReviewData review, GeneralComment comment, AtlassianClickAction action) {
		super(action);
		this.review = review;
		this.comment = comment;
	}

	public ReviewData getReview() {
		return review;
	}

	public GeneralComment getComment() {
		return comment;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	private static class MyRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) value;
			JPanel panel;
			if (node.isEditable()) {
				panel = CommentPanelBuilder.createEditPanelOfGeneralComment(
						node.getReview(), node.getComment());
			} else {
				panel = CommentPanelBuilder.createViewPanelOfGeneralComment(
						node.getReview(), node.getComment());
			}
			panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
					: BorderFactory.createEmptyBorder(1,1,1,1));
			return panel;

		}
	}
}
