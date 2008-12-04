/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewTreeModel extends DefaultTreeModel implements CrucibleReviewListModelListener {

	private CrucibleReviewListModel reviewListModel;
	private boolean treeInitialized = false;

	public ReviewTreeModel(CrucibleReviewListModel reviewListModel) {
		super(new CrucibleReviewGroupTreeNode(reviewListModel, "No Grouping At All", null, null));
		this.reviewListModel = reviewListModel;

		reviewListModel.addListener(this);
	}

	/*
	Override TreeModel methods
	 */

	public Object getRoot() {
		return super.getRoot();
	}

	public Object getChild(Object parent, int index) {
		if (parent instanceof CrucibleReviewGroupTreeNode && parent == root) {
			ReviewAdapter r = (ReviewAdapter) reviewListModel.getReviews().toArray()[index];
			if (r != null) {
				CrucibleReviewGroupTreeNode p = (CrucibleReviewGroupTreeNode) parent;
				if (index < p.getChildCount()) {
					return p.getChildAt(index);
				}

				CrucibleReviewTreeNode n = new CrucibleReviewTreeNode(reviewListModel, r);
				p.add(n);
				return n;
			}
		}
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent instanceof CrucibleReviewGroupTreeNode && parent == root) {
			return reviewListModel.getReviews().size();
		}

		return 0;
	}

	public boolean isLeaf(Object node) {
		if (node == super.getRoot()) {
			return false;
		}

		return true;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("valueForPathChanged");
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof CrucibleReviewGroupTreeNode && parent == root) {
			if (child instanceof CrucibleReviewTreeNode) {
				ReviewAdapter review = ((CrucibleReviewTreeNode) child).getReview();
				return new ArrayList<ReviewAdapter>(reviewListModel.getReviews()).indexOf(review);
			}
		}

		return -1;
	}

	/*
	Implement CrucibleReviewListModelListener interface
	 */

	public void reviewAdded(ReviewAdapter review) {
		System.out.println("review added");

		if (treeInitialized) {
			this.nodeStructureChanged(root);
		}
	}

	public void reviewRemoved(ReviewAdapter review) {
		System.out.println("review removed");

		if (treeInitialized) {
			this.nodeStructureChanged(root);
		}
	}

	public void reviewChanged(ReviewAdapter review) {
		System.out.println("review changed");
	}

	public void reviewListUpdateStarted(ServerId serverId) {
		System.out.println("reviews update started");
	}

	public void reviewListUpdateFinished(ServerId serverId) {
		System.out.println("reviews updated finished");

		if (!treeInitialized) {
			// draw entire tree
			this.nodeStructureChanged(root);
			treeInitialized = true;
		}
	}

	public void reviewChangedWithoutFiles(ReviewAdapter newReview) {
		System.out.println("review changed without files");
		
		if (treeInitialized) {
			this.nodeStructureChanged(root);
		}
	}
}
