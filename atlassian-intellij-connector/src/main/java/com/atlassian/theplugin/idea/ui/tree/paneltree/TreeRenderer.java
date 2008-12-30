package com.atlassian.theplugin.idea.ui.tree.paneltree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class TreeRenderer extends DefaultTreeCellRenderer {
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
	                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {

		JComponent c = (JComponent) super.getTreeCellRendererComponent(
		        tree, value, selected, expanded, leaf, row, hasFocus);

		if (value instanceof AbstractTreeNode) {
			return ((AbstractTreeNode) value).getRenderer(c, selected, expanded, hasFocus);
		}
		return c;
	}
}
