package com.atlassian.theplugin.idea.jira.tree;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.*;
import java.awt.*;

public class JIRAIssueTreeRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
	                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {

		JComponent c = (JComponent) super.getTreeCellRendererComponent(
		        tree, value, selected, expanded, leaf, row, hasFocus);

		if (value instanceof JIRAIssueAbstractTreeNode) {
			return ((JIRAIssueAbstractTreeNode) value).getRenderer(c, selected, expanded, hasFocus);
		}
		return c;
	}
}
