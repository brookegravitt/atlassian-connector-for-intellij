package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class CruciblePredefinedFilterTreeNode extends AbstractTreeNode {
	private PredefinedFilter filter;

	CruciblePredefinedFilterTreeNode(PredefinedFilter filter) {
		super(filter.getFilterName(), null, null);
		this.filter = filter;
	}

	public String toString() {
		return filter.getFilterName();
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return new JLabel(filter.getFilterName());
	}

	public void onSelect() {
	}

	public PredefinedFilter getFilter() {
		return filter;
	}
}
