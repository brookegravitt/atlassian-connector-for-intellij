package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterTreeNode  extends AbstractTreeNode {
	private CustomFilter filter;

	public CrucibleCustomFilterTreeNode(CustomFilter filter) {
		super("Custom Filter", null, null);
		this.filter = filter;
	}
	public String toString() {
		return "Custom Filter";
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return new JLabel("Custom Filter");
	}

	public void onSelect() {
	}

	public CustomFilter getFilter() {
		return filter;
	}
}
