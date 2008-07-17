package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.util.CommentPanelBuilder;

import javax.swing.tree.TreeCellRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 11:13:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentTreeNode extends CommentTreeNode {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedComment comment;
	private static final TreeCellRenderer MY_RENDERER = new MyTreeRenderer();

	public VersionedCommentTreeNode(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
		super();
		this.review = review;
		this.file = file;
		this.comment = comment;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public ReviewData getReview() {
		return review;
	}

	public CrucibleFileInfo getFile() {
		return file;
	}

	public VersionedComment getComment() {
		return comment;
	}

	private static class MyTreeRenderer implements TreeCellRenderer {


		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) value;
			JPanel panel;
			if (node.isEditable()) {
				panel = CommentPanelBuilder.createEditPanelOfVersionedComment(
						node.getReview(), node.getFile(), node.getComment());
			} else {
				panel = CommentPanelBuilder.createViewPanelOfVersionedComment(
						node.getReview(), node.getFile(), node.getComment());
			}
			panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
					: BorderFactory.createEmptyBorder(1,1,1,1));
			return panel;
		}
	}
}
