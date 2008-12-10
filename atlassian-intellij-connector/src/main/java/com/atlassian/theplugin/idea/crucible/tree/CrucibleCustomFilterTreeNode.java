package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterTreeNode  extends AbstractTreeNode {
	private CustomFilter filter;
	private CrucibleFilterListModel listModel;

	public CrucibleCustomFilterTreeNode(CrucibleFilterListModel listModel, CustomFilter filter) {
		super("Custom Filter", null, null);
		this.listModel = listModel;
		this.filter = filter;
	}
	public String toString() {
		return "Custom Filter";
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return new JLabel("Custom Filter");
	}

	public void onSelect() {
		listModel.setSelectedCustomFilter(filter);
	}

	public CustomFilter getFilter() {
		return filter;
	}
}
