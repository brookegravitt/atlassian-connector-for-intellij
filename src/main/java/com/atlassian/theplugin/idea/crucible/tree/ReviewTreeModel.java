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
	private NodeManipulator serverNodeManupulator;

	public ReviewTreeModel(CrucibleReviewListModel reviewListModel) {
		super(new DefaultMutableTreeNode());

		this.reviewListModel = reviewListModel;

		generalNodeManipulator = new GeneralNodeManipulator(reviewListModel, getRoot());
		stateNodeManipulator = new StateNodeManipulator(reviewListModel, getRoot());
		serverNodeManupulator = new ServerNodeManipulator(reviewListModel, getRoot());

		reviewListModel.addListener(localModelListener);
	}

	public void groupBy(CrucibleReviewGroupBy aGroupBy) {
		this.groupBy = aGroupBy;

		// clear entire tree
		getRoot().removeAllChildren();

		// redraw tree
		nodeStructureChanged(getRoot());
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
				break;
			case PROJECT:
				break;
			case SERVER:
				return serverNodeManupulator.getChild(parent, index);
			case STATE:
				return stateNodeManipulator.getChild(parent, index);
			case NONE:
			default:
				return generalNodeManipulator.getChild(parent, index);
		}

		return null;
	}

	@Override
	public int getChildCount(Object parent) {

		switch (groupBy) {
			case AUTHOR:
				break;
			case PROJECT:
				break;
			case SERVER:
				return serverNodeManupulator.getChildCount(parent);
			case STATE:
				return stateNodeManipulator.getChildCount(parent);
			case NONE:
			default:
				return generalNodeManipulator.getChildCount(parent);
		}

		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == getRoot()
				|| node instanceof CrucibleReviewStateTreeNode
				|| node instanceof CrucibleReviewServerTreeNode) {
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
			System.out.println("review added");

			// todo add implementation

			if (treeInitialized) {
				nodeStructureChanged(getRoot());
			}
		}

		@Override
		public void reviewRemoved(ReviewAdapter review) {
			System.out.println("review removed");

			// todo add implementation

			if (treeInitialized) {
				nodeStructureChanged(getRoot());
			}
		}

		@Override
		public void reviewChangedWithoutFiles(ReviewAdapter newReview) {
			System.out.println("review changed without files");

			// todo add implementation

			if (treeInitialized) {
				nodeStructureChanged(getRoot());
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
//				nodeStructureChanged(root);
				treeInitialized = true;
			}

			// todo remove that example groupBy call and uncomment above 
			groupBy(CrucibleReviewGroupBy.STATE);
		}
	}

}
