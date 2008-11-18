package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.intellij.util.Icons;

import javax.swing.*;

public class JIRAIssueGroupTreeNode extends JIRAAbstractTreeNode {
	private final JIRAIssueListModel model;
	private final String name;
	private final Icon iconOpen;
	private final Icon iconClosed;

	public JIRAIssueGroupTreeNode(JIRAIssueListModel model, String name, Icon icon) {
		this.model = model;
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
		l.setText("<html><b>" + name + " (" + getChildCount() + ")</b>");
		return l;
	}

	public void onSelect() {
		model.setSeletedIssue(null);
	}

	public String toString() {
		return name;
	}
}
