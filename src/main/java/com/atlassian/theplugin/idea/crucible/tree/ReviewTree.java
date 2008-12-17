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

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListenerAdapter;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewTree extends JTree implements TreeModelListener {
	private ReviewTreeModel model;
	private CrucibleReviewListModelListener localModelListener = new LocalCrucibeReviewListModelListener();

	public ReviewTree(ReviewTreeModel reviewTreeModel) {
		super(reviewTreeModel);

		this.model = reviewTreeModel;

		// listen to the review tree model changes
		reviewTreeModel.addTreeModelListener(this);
		// listen to the global review list changes
		reviewTreeModel.getReviewListModel().addListener(localModelListener);

		init();
	}

	private void init() {
		setRootVisible(false);
		setShowsRootHandles(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	public void treeNodesChanged(TreeModelEvent e) {
//		System.out.println("tree nodes changed");
	}

	public void treeNodesInserted(TreeModelEvent e) {
//		System.out.println("tree nodes inserted");
	}

	public void treeNodesRemoved(TreeModelEvent e) {
//		System.out.println("tree nodes remmoved");
	}

	public void treeStructureChanged(TreeModelEvent e) {
//		System.out.println("tree structure changed");

		expandTree();
	}

	private void expandTree() {
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	public ReviewAdapter getSelectedReview() {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
			return ((ReviewTreeNode) selectionPath.getLastPathComponent()).getReview();
		} else {
			// nothing selected
			return null;
		}
	}


	/*
	Listen to the review list model changes
	 */
	private class LocalCrucibeReviewListModelListener extends CrucibleReviewListModelListenerAdapter {

		private boolean treeChanged = false;
		private boolean treeInitialized = false;

		@Override
		public void reviewAdded(ReviewAdapter review) {
//			System.out.println("review added: " + review.getPermId().getId());

//			treeChanged = true;

			if (treeInitialized) {
				fireTreeChanged(model.findParentNode(review));
			}
		}

		@Override
		public void reviewRemoved(ReviewAdapter review) {
//			System.out.println("review removed: " + review.getPermId().getId());

//			treeChanged = true;

			if (treeInitialized) {
				fireTreeChanged(model.findParentNode(review));
			}
		}

		@Override
		public void reviewChangedWithoutFiles(ReviewAdapter review) {
//			System.out.println("review changed without files: " + review.getPermId().getId());

			treeChanged = true;
		}

		@Override
		public void reviewListUpdateStarted() {
//			System.out.println("reviews update started");

			// reset tree state
			treeChanged = false;
		}

		@Override
		public void reviewListUpdateFinished() {
//			System.out.println("reviews updated finished");

			if (treeChanged || !treeInitialized) {
				// draw entire tree
				fireTreeChanged(model.getRoot());
				treeChanged = false;
				treeInitialized = true;
			}
		}

		@Override
		public void modelChanged() {
			fireTreeChanged(model.getRoot());
		}

		private void fireTreeChanged(DefaultMutableTreeNode node) {
			node.removeAllChildren();
			model.nodeStructureChanged(node);
		}

	}
}
