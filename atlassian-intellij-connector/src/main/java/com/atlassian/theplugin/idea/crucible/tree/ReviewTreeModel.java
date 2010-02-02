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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewGroupBy;
import com.atlassian.theplugin.idea.crucible.tree.node.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewTreeModel extends DefaultTreeModel {

	private CrucibleReviewListModel reviewListModel;


	private CrucibleReviewGroupBy groupBy = CrucibleReviewGroupBy.NONE;

	private NodeManipulator generalNodeManipulator;
	private NodeManipulator stateNodeManipulator;
	private NodeManipulator serverNodeManipulator;
	private NodeManipulator authorNodeManipulator;
	private NodeManipulator projectNodeManipulator;

	public ReviewTreeModel(CrucibleReviewListModel reviewListModel, @NotNull ProjectCfgManager projectCfgManager) {
		super(new DefaultMutableTreeNode());

		this.reviewListModel = reviewListModel;

		generalNodeManipulator = new GeneralNodeManipulator(reviewListModel, getRoot());
		stateNodeManipulator = new StateNodeManipulator(reviewListModel, getRoot());
		serverNodeManipulator = new ServerNodeManipulator(projectCfgManager, reviewListModel, getRoot());
		authorNodeManipulator = new AuthorNodeManipulator(reviewListModel, getRoot());
		projectNodeManipulator = new ProjectNodeManipulator(reviewListModel, getRoot());
	}

	/**
	 * Sets groupBy field used to group the tree and triggers tree to rebuild
	 * Only tree should use that method.
	 *
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
	 *
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

	public DefaultMutableTreeNode findParentNode(ReviewAdapter review) {
		DefaultMutableTreeNode root = getRoot();

		switch (groupBy) {

			case AUTHOR:
				// todo move that code to node manipulator and make it generic if worth
				for (int i = 0; i < getChildCount(root); ++i) {
					Object node = getChild(root, i);
					if (node instanceof CrucibleReviewAuthorTreeNode) {
						CrucibleReviewAuthorTreeNode parent = (CrucibleReviewAuthorTreeNode) node;
						if (parent.getAuthor().equals(review.getAuthor())) {
							return parent;
						}
					}
				}
				break;
			case PROJECT:
				for (int i = 0; i < getChildCount(root); ++i) {
					Object node = getChild(root, i);
					if (node instanceof CrucibleReviewProjectTreeNode) {
						CrucibleReviewProjectTreeNode parent = (CrucibleReviewProjectTreeNode) node;
						if (parent.getProject().getKey().equals(review.getProjectKey())) {
							return parent;
						}
					}
				}
				break;
			case SERVER:
				for (int i = 0; i < getChildCount(root); ++i) {
					Object node = getChild(root, i);
					if (node instanceof CrucibleReviewServerTreeNode) {
						CrucibleReviewServerTreeNode parent = (CrucibleReviewServerTreeNode) node;
						if (parent.getCrucibleServer().getServerId()
								.equals(review.getServerData().getServerId())) {
							return parent;
						}
					}
				}
				break;
			case STATE:
				for (int i = 0; i < getChildCount(root); ++i) {
					Object node = getChild(root, i);
					if (node instanceof CrucibleReviewStateTreeNode) {
						CrucibleReviewStateTreeNode parent = (CrucibleReviewStateTreeNode) node;
						if (parent.getCrucibleState().equals(review.getState())) {
							return parent;
						}
					}
				}
				break;
			case NONE:
			default:
				break;
		}

		return root;
	}

	public CrucibleReviewListModel getReviewListModel() {
		return reviewListModel;
	}
}
