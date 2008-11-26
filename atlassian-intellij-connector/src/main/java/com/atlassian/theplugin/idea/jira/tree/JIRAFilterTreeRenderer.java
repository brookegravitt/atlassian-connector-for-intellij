package com.atlassian.theplugin.idea.jira.tree;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class JIRAFilterTreeRenderer extends DefaultTreeCellRenderer {
		private static final Icon JIRA_MANUAL_FILTER_ICON = IconLoader.getIcon("/actions/showViewer.png");
	private static final Icon JIRA_SAVED_FILTER_ICON = IconLoader.getIcon("/actions/showSource.png");

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
	                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {

		JComponent c = (JComponent) super.getTreeCellRendererComponent(
		        tree, value, selected, expanded, leaf, row, hasFocus);

		if (value instanceof JIRAManualFilterTreeNode && c instanceof JLabel) {
			((JLabel)c).setIcon(JIRA_MANUAL_FILTER_ICON);
			
			return c;

		}

		if (value instanceof JIRASavedFilterTreeNode && c instanceof JLabel) {
			((JLabel)c).setIcon(JIRA_SAVED_FILTER_ICON);
			return c;

		}
		
		if (value instanceof JIRAAbstractTreeNode) {
			return ((JIRAAbstractTreeNode) value).getRenderer(c, selected, expanded, hasFocus);
		}
		return c;
	}
}
