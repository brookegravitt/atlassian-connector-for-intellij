package com.atlassian.theplugin.idea.jira.tree;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

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
		SimpleColoredComponent component = new SimpleColoredComponent();
		component.append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES);

		component.setIcon(JIRA_FILTER_ICON);
		component.setOpaque(true);
		component.setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
		component.setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
		return component;
	}

	public void onSelect() {
		
	}
}
