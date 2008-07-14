package com.atlassian.theplugin.idea.ui.tree;

import com.intellij.ui.ColoredTreeCellRenderer;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 11, 2008
 * Time: 1:22:36 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AtlassianTreeNode extends DefaultMutableTreeNode {
	protected AtlassianTreeNode(Object root) {
		super(root);
	}

	public abstract ColoredTreeCellRenderer getTreeCellRenderer();
}
