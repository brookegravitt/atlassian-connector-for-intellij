package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;

import javax.swing.tree.TreeCellRenderer;
import javax.swing.*;

public abstract class CrucibleContainerNode extends FileNode {
	private ReviewData review;

	public CrucibleContainerNode(ReviewData review) {
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

	protected ReviewData getReview() {
		return review;
	}
}
