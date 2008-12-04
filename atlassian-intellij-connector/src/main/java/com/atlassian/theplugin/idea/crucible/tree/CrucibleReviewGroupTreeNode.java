package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

/**
 * User: jgorycki
 * Date: Dec 4, 2008
 * Time: 11:40:13 AM
 */
public class CrucibleReviewGroupTreeNode extends AbstractTreeNode {
	private final CrucibleReviewListModel model;

	public CrucibleReviewGroupTreeNode(CrucibleReviewListModel model, String name, Icon icon, Icon disabledIcon) {
		super(name, icon,  disabledIcon);
		this.model = model;
	}

	public String toString() {
		return name;
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return getDefaultRenderer(c, selected, expanded, hasFocus);
	}

	public void onSelect() {
		model.setSelectedReview(null);
	}
}
