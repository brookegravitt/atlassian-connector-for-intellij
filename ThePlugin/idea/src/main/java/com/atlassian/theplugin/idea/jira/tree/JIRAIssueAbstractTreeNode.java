package com.atlassian.theplugin.idea.jira.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;

public abstract class JIRAIssueAbstractTreeNode extends DefaultMutableTreeNode {
	public abstract String toString();
	public abstract JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus);
}
