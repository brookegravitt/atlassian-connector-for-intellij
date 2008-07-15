package com.atlassian.theplugin.idea.crucible;


import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 10, 2008
 * Time: 3:06:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleReviewTreeModel extends DefaultTreeModel {

	static final long serialVersionUID = 1631701743528670523L;

	public CrucibleReviewTreeModel(CrucibleTreeRootNode root) {
		super(root);
	}

	public ReviewItemDataNode getReviewItemDataNode(CrucibleFileInfo reviewItem, boolean addIfMissing) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			ReviewItemDataNode reviewItemDataNode = (ReviewItemDataNode) root.getChildAt(i);
			if (reviewItemDataNode.getFile() == reviewItem) {
				return reviewItemDataNode;
			}
		}
		if (addIfMissing) {
			final ReviewItemDataNode child = new ReviewItemDataNode(reviewItem);
			insertNodeInto(child, (DefaultMutableTreeNode) root, root.getChildCount());
			this.nodeChanged(root);
			return child;
		}
		return null;
	}

}

