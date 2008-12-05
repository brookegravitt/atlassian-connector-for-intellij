package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class CruciblePredefinedFilterTreeNode extends AbstractTreeNode {
	private CrucibleFilterListModel listModel;
	private PredefinedFilter filter;

	CruciblePredefinedFilterTreeNode(CrucibleFilterListModel listModel, PredefinedFilter filter) {
		super(filter.getFilterName(), null, null);
		this.listModel = listModel;
		this.filter = filter;
	}

	public String toString() {
		return filter.getFilterName();
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return new JLabel(filter.getFilterName());
	}

	public void onSelect() {
		listModel.setSelectedPredefinedFilter(filter);
	}

	public PredefinedFilter getFilter() {
		return filter;
	}
}
