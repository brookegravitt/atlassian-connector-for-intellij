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
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListenerAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewGroupBy;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewTreeModel extends DefaultTreeModel {

	private CrucibleReviewListModel reviewListModel;
	private boolean treeInitialized = false;

	private CrucibleReviewListModelListener modelListener = new LocalCrucibeReviewListModelListener();
	private CrucibleReviewGroupBy groupBy = CrucibleReviewGroupBy.NONE;

	public ReviewTreeModel(CrucibleReviewListModel reviewListModel) {
		super(new CrucibleReviewGroupTreeNode(reviewListModel, "No Grouping At All", null, null));
		this.reviewListModel = reviewListModel;

		reviewListModel.addListener(modelListener);
	}

	public void groupBy(CrucibleReviewGroupBy aGroupBy) {
		this.groupBy = aGroupBy;

		nodeStructureChanged(root);
	}

	/*
	Override TreeModel methods
	 */

	@Override
	public Object getRoot() {
		return super.getRoot();
	}

	@Override
	public Object getChild(Object parent, int index) {

		switch (groupBy) {

			case AUTHOR:
				break;
			case PROJECT:
				break;
			case SERVER:
				break;
			case STATE:

				if (parent instanceof CrucibleReviewGroupTreeNode && parent == root) {

					CrucibleReviewGroupTreeNode p = (CrucibleReviewGroupTreeNode) parent;

					if (index < p.getChildCount()) {
						return p.getChildAt(index);
					}

					State state = State.values()[index];

					CrucibleReviewStateTreeNode stateNode = new CrucibleReviewStateTreeNode(reviewListModel, state);
					p.add(stateNode);
					return stateNode;

				} else if (parent instanceof CrucibleReviewStateTreeNode) {
					CrucibleReviewStateTreeNode p = (CrucibleReviewStateTreeNode) parent;

					if (index < p.getChildCount()) {
						return p.getChildAt(index);
					}

					ReviewAdapter review = getReviewInState(p.getCrucibleState(), index);
					CrucibleReviewTreeNode node = new CrucibleReviewTreeNode(reviewListModel, review);
					p.add(node);

					return node;
				}

				break;
			case NONE:
			default:

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
		}

		return null;
	}

	private ReviewAdapter getReviewInState(State crucibleState, int index) {
		List<ReviewAdapter> array = new ArrayList<ReviewAdapter>();
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getState() == crucibleState) {
				array.add(review);
			}
		}

		return array.get(index);
	}

	@Override
	public int getChildCount(Object parent) {
		int childCount = 0;

		switch (groupBy) {
			case AUTHOR:
				break;
			case PROJECT:
				break;
			case SERVER:
				break;
			case STATE:
				if (parent instanceof CrucibleReviewGroupTreeNode && parent == root) {
					childCount = getNumOfDistinctStates();
				} else if (parent instanceof CrucibleReviewStateTreeNode) {
					CrucibleReviewStateTreeNode stateNode = (CrucibleReviewStateTreeNode) parent;
					childCount = gentNumOfReviewsInState(stateNode.getCrucibleState());
				}
				break;
			case NONE:
			default:
				if (parent instanceof CrucibleReviewGroupTreeNode && parent == root) {
					childCount = reviewListModel.getReviews().size();
				}
		}

		return childCount;
	}

	private int gentNumOfReviewsInState(State crucibleState) {
		int ret = 0;
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getState() == crucibleState) {
				++ret;
			}
		}

		return ret;
	}

	private int getNumOfDistinctStates() {
		// todo return number of distinct states (not all as it is now)
		return State.values().length;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == super.getRoot()
				|| node instanceof CrucibleReviewStateTreeNode) {
			return false;
		}

		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("valueForPathChanged");
	}

	@Override
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
	Listen to the review list model changes
	 */
	private class LocalCrucibeReviewListModelListener extends CrucibleReviewListModelListenerAdapter {

		@Override
		public void reviewAdded(ReviewAdapter review) {
			System.out.println("review added");

			if (treeInitialized) {
				nodeStructureChanged(root);
			}
		}

		@Override
		public void reviewRemoved(ReviewAdapter review) {
			System.out.println("review removed");

			if (treeInitialized) {
				nodeStructureChanged(root);
			}
		}

		@Override
		public void reviewChangedWithoutFiles(ReviewAdapter newReview) {
			System.out.println("review changed without files");

			if (treeInitialized) {
				nodeStructureChanged(root);
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

			// todo remove that example groupBy call
			groupBy(CrucibleReviewGroupBy.STATE);
		}
	}

}
