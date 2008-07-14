package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

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

	public ReviewItemDataNode getOrInsertReviewItemDataNode(ReviewItem reviewItem, boolean addIfMissing) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			if (root.getChildAt(i) instanceof ReviewItemDataNode) {
				ReviewItemDataNode reviewItemDataNode = (ReviewItemDataNode) root.getChildAt(i);
				if (reviewItemDataNode.getReviewItem() == reviewItem) {
						return reviewItemDataNode;
				}
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

	public GeneralCommentNode getGeneralCommentNode(GeneralComment generalComment, boolean addIfMissing) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			if (root.getChildAt(i) instanceof GeneralComment) {
				GeneralCommentNode node = (GeneralCommentNode) root.getChildAt(i);
					if (node.getGeneralComment() == generalComment) {
						return node;
				}

			}
		}
		if (addIfMissing) {

				final GeneralCommentNode child = new GeneralCommentNode(generalComment);
				insertNodeInto(child, (DefaultMutableTreeNode) root, root.getChildCount());
				this.nodeChanged(root);
				return child;
		}
		return null;
	}
 
}

