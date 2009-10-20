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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListenerAdapter;
import com.atlassian.theplugin.crucible.model.UpdateContext;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewGroupBy;
import com.atlassian.theplugin.idea.crucible.tree.node.CrucibleReviewTreeNode;
import com.atlassian.theplugin.idea.ui.tree.AbstractTree;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewTree extends AbstractTree {
	private ReviewTreeModel model;
	private CrucibleReviewListModelListener localReviewModelListener = new LocalCrucibeReviewListModelListener();
	private TreeModelListener localTreeModelListener = new LocalTreeModelListener();

	public ReviewTree(ReviewTreeModel reviewTreeModel) {
		super(reviewTreeModel);

		this.model = reviewTreeModel;

		// listen to the review tree model changes
		reviewTreeModel.addTreeModelListener(localTreeModelListener);
		// listen to the global review list changes
		reviewTreeModel.getReviewListModel().addListener(localReviewModelListener);

		init();
	}

	private void init() {
		setRootVisible(false);
		setShowsRootHandles(true);
		setExpandsSelectedPaths(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
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

	private void selectReviewNode(ReviewAdapter review) {
		if (review == null) {
			clearSelection();
			return;
		}

		for (int i = 0; i < getRowCount(); i++) {
			TreePath path = getPathForRow(i);
			Object object = path.getLastPathComponent();
			if (object instanceof CrucibleReviewTreeNode) {
				CrucibleReviewTreeNode node = (CrucibleReviewTreeNode) object;
				if (node.getReview().getPermId().equals(review.getPermId())) {
					expandPath(path);
					makeVisible(path);
					setSelectionPath(path);
					break;
				}
			}
		}
	}


	public void groupBy(CrucibleReviewGroupBy groupBy) {
		model.groupBy(groupBy);
		expandTree();
	}

	public void setGroupBy(CrucibleReviewGroupBy groupBy) {
		model.setGroupBy(groupBy);
	}

	/*
	Listen to the review list changes
	*/
	private class LocalCrucibeReviewListModelListener extends CrucibleReviewListModelListenerAdapter {

		private boolean treeChanged = false;

		private boolean treeInitialized = false;

		private Set<DefaultMutableTreeNode> changedNodes = new HashSet<DefaultMutableTreeNode>();

		@Override
		public void reviewAdded(UpdateContext updateContext) {
//			System.out.println("review added: " + review.getPermId().getId());

			treeChanged = true;

//			if (treeInitialized) {
//				changedNodes.add(model.findParentNode(review));
////				fireTreeChanged(model.findParentNode(review));
//			}
		}

		@Override
		public void reviewRemoved(UpdateContext updateContext) {
//			System.out.println("review removed: " + review.getPermId().getId());

			treeChanged = true;

//			if (treeInitialized) {
//				changedNodes.add(model.findParentNode(review));
////				fireTreeChanged(model.findParentNode(review));
//			}
		}

		@Override
		public void reviewChanged(UpdateContext updateContext) {
//			System.out.println("review changed without files: " + review.getPermId().getId());

			treeChanged = true;
		}

		@Override
		public void reviewListUpdateStarted(UpdateContext updateContext) {
//			System.out.println("reviews update started");

			// reset tree state
			treeChanged = false;
		}

		@Override
		public void reviewListUpdateFinished(UpdateContext updateContext) {
//			System.out.println("reviews updated finished");

			if (treeChanged || !treeInitialized) {
				// draw entire tree
				fireTreeChanged(model.getRoot());
				treeChanged = false;
				treeInitialized = true;
			}
//			else if (changedNodes.size() > 0) {
//				for (DefaultMutableTreeNode node : changedNodes) {
//					fireTreeChanged(node);
//				}
//				changedNodes.clear();
//			}
		}

		@Override
		public void modelChanged(UpdateContext updateContext) {
			fireTreeChanged(model.getRoot());
		}

		private void fireTreeChanged(DefaultMutableTreeNode node) {
			// remember selection and collapse state
			Set<TreePath> collapsedPaths = getCollapsedPaths();
			ReviewAdapter review = getSelectedReview();

			// rebuild the tree
			node.removeAllChildren();
			model.nodeStructureChanged(node);
			// expand entire tree
			expandTree();

			// restore selection and collapse state
			collapsePaths(collapsedPaths);
			selectReviewNode(review);
		}

	}


	private class LocalTreeModelListener implements TreeModelListener {

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
	}
}
