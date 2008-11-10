package com.atlassian.theplugin.idea.jira.tree;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRAManualFilterTreeNode extends JIRAAbstractTreeNode {
	private static final Icon JIRA_FILTER_ICON = IconLoader.getIcon("/actions/showViewer.png");
	private String name;

	public JIRAManualFilterTreeNode(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
	                              final boolean expanded, final boolean hasFocus) {

		JLabel label = new JLabel(name, JIRA_FILTER_ICON, SwingUtilities.HORIZONTAL);		
		return label;
	}

	public void onSelect() {
		
	}
}
