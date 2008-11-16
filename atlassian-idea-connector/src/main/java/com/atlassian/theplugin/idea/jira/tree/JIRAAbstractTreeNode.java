package com.atlassian.theplugin.idea.jira.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class JIRAAbstractTreeNode extends DefaultMutableTreeNode {
	public abstract String toString();
	public abstract JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus);
	public abstract void onSelect();
}
