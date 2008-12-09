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
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListenerAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewGroupBy;
import com.atlassian.theplugin.idea.crucible.tree.node.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewTreeModel extends DefaultTreeModel {

	private CrucibleReviewListModel reviewListModel;
	private boolean treeInitialized = false;

	private CrucibleReviewListModelListener localModelListener = new LocalCrucibeReviewListModelListener();
	private CrucibleReviewGroupBy groupBy = CrucibleReviewGroupBy.NONE;

	private NodeManipulator generalNodeManipulator;
	private NodeManipulator stateNodeManipulator;
	private NodeManipulator serverNodeManipulator;
	private NodeManipulator authorNodeManipulator;
	private NodeManipulator projectNodeManipulator;

	public ReviewTreeModel(CrucibleReviewListModel reviewListModel) {
		super(new DefaultMutableTreeNode());

		this.reviewListModel = reviewListModel;

		generalNodeManipulator = new GeneralNodeManipulator(reviewListModel, getRoot());
		stateNodeManipulator = new StateNodeManipulator(reviewListModel, getRoot());
		serverNodeManipulator = new ServerNodeManipulator(reviewListModel, getRoot());
		authorNodeManipulator = new AuthorNodeManipulator(reviewListModel, getRoot());
		projectNodeManipulator = new ProjectNodeManipulator(reviewListModel, getRoot());

		reviewListModel.addListener(localModelListener);
	}

	/**
	 * Sets groupBy field used to group the tree and triggers tree to rebuild
	 * @param aGroupBy
	 */
	public void groupBy(CrucibleReviewGroupBy aGroupBy) {
		this.groupBy = aGroupBy;

		// clear entire tree
		getRoot().removeAllChildren();

		// redraw tree
		nodeStructureChanged(getRoot());
	}

	/**
	 * Simple setter (does not trigger tree to rebuild)
	 * @param groupBy
	 */
	public void setGroupBy(CrucibleReviewGroupBy groupBy) {
		this.groupBy = groupBy;
	}

	/*
	Override TreeModel methods
	 */

	@Override
	public DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) super.getRoot();
	}

	@Override
	public Object getChild(Object parent, int index) {

		switch (groupBy) {
			case AUTHOR:
				return authorNodeManipulator.getChild(parent, index);
			case PROJECT:
				return projectNodeManipulator.getChild(parent, index);
			case SERVER:
				return serverNodeManipulator.getChild(parent, index);
			case STATE:
				return stateNodeManipulator.getChild(parent, index);
			case NONE:
			default:
				return generalNodeManipulator.getChild(parent, index);
		}
	}

	@Override
	public int getChildCount(Object parent) {

		switch (groupBy) {
			case AUTHOR:
				return authorNodeManipulator.getChildCount(parent);
			case PROJECT:
				return projectNodeManipulator.getChildCount(parent);
			case SERVER:
				return serverNodeManipulator.getChildCount(parent);
			case STATE:
				return stateNodeManipulator.getChildCount(parent);
			case NONE:
			default:
				return generalNodeManipulator.getChildCount(parent);
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == getRoot()
				|| node instanceof CrucibleReviewStateTreeNode
				|| node instanceof CrucibleReviewServerTreeNode
				|| node instanceof CrucibleReviewAuthorTreeNode
				|| node instanceof CrucibleReviewProjectTreeNode) {
			return false;
		}

		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("valueForPathChanged");
	}

	@Override
	// todo add group by handling if necessary
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == getRoot()) {
			if (child instanceof CrucibleReviewTreeNode) {
				ReviewAdapter review = ((CrucibleReviewTreeNode) child).getReview();
				return new ArrayList<ReviewAdapter>(reviewListModel.getReviews()).indexOf(review);
			}
		}

		return -1;
	}

	/*
	Listen to the review list model changes
	 */
	private class LocalCrucibeReviewListModelListener extends CrucibleReviewListModelListenerAdapter {

		@Override
		public void reviewAdded(ReviewAdapter review) {
			System.out.println("review added: " + review.getPermId().getId());

			// todo add implementation

			if (treeInitialized) {
				fireTreeChanged(getRoot());
			}
		}

		@Override
		public void reviewRemoved(ReviewAdapter review) {
			System.out.println("review removed: " + review.getPermId().getId());

			// todo add implementation

			if (treeInitialized) {
				fireTreeChanged(getRoot());
			}
		}

		@Override
		public void reviewChangedWithoutFiles(ReviewAdapter review) {
			System.out.println("review changed without files: " + review.getPermId().getId());

			// todo add implementation

			if (treeInitialized) {
				fireTreeChanged(getRoot());
			}
		}

		@Override
		public void reviewListUpdateStarted(ServerId serverId) {
			System.out.println("reviews update started");
		}

		@Override
		public void reviewListUpdateFinished(ServerId serverId) {
			System.out.println("reviews updated finished");

			if (!treeInitialized) {
				// draw entire tree
				fireTreeChanged(getRoot());
				treeInitialized = true;
			}
		}

		private void fireTreeChanged(DefaultMutableTreeNode node) {
			node.removeAllChildren();
			nodeStructureChanged(node);
		}

	}

}
