package com.atlassian.theplugin.idea.ui.tree;

import com.intellij.ui.ColoredTreeCellRenderer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 11, 2008
 * Time: 1:22:36 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AtlassianTreeNode extends DefaultMutableTreeNode {
	private AtlassianClickAction action;

	protected AtlassianTreeNode(AtlassianClickAction action) {
		super();
		this.action = action;
	}

	public void addNode(AtlassianTreeNode newChild) {
		super.add(newChild);	//To change body of overridden methods use File | Settings | File Templates.
	}

	public abstract TreeCellRenderer getTreeCellRenderer();

	public AtlassianClickAction getAtlassianClickAction() {
		return action;
	}

}
