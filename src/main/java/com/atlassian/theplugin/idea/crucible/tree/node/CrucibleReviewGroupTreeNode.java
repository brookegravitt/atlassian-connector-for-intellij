package com.atlassian.theplugin.idea.crucible.tree.node;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTreeNode;

import javax.swing.*;

/**
 * User: jgorycki
 * Date: Dec 4, 2008
 * Time: 11:40:13 AM
 */
public class CrucibleReviewGroupTreeNode extends ReviewTreeNode {

	public CrucibleReviewGroupTreeNode(String name, Icon icon, Icon disabledIcon) {
		super(name, icon,  disabledIcon);
	}

	public String toString() {
		return name;
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return getDefaultRenderer(c, selected, expanded, hasFocus);
	}

	public void onSelect() {
//		model.setSelectedReview(null); <- selection is stored inside the tree instead of global plugin review model
	}

	public ReviewAdapter getReview() {
		return null;	// no review associated with this node
	}
}
