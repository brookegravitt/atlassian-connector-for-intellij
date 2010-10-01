package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

public abstract class CrucibleContainerNode extends FileNode {
	private ReviewAdapter review;

	public CrucibleContainerNode(ReviewAdapter review) {
		super("container", null);
		this.review = review;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return new ColoredTreeCellRenderer() {
			public void customizeCellRenderer(
					JTree tree, Object value, boolean selected,
					boolean expanded, boolean leaf, int row, boolean hasFocus) {
				append(getText(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, null));
				setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
			}
		};
	}

	protected abstract String getText();

	public ReviewAdapter getReview() {
		return review;
	}

	public void setReview(ReviewAdapter review) {
		this.review = review;
	}
}
