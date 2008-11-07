package com.atlassian.theplugin.idea.jira.tree;

import com.intellij.util.Icons;

import javax.swing.*;

public class JIRAGroupTreeNode extends JIRAAbstractTreeNode {
	private final String name;
	private final Icon iconOpen;
	private final Icon iconClosed;

	public JIRAGroupTreeNode(String name, Icon icon) {
		this.name = name;
		if (icon != null) {
			this.iconOpen = icon;
			this.iconClosed = icon;
		} else {
			this.iconOpen = Icons.DIRECTORY_OPEN_ICON;
			this.iconClosed = Icons.DIRECTORY_CLOSED_ICON;
		}
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {

		JLabel l = (JLabel) c;
		l.setIcon(expanded ? iconOpen : iconClosed);
		l.setText(name);
		return l;
	}

	public void onSelect() {		
	}

	public String toString() {
		return name;
	}
}
