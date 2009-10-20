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
package com.atlassian.theplugin.idea.crucible.tree.node;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * @author Jacek Jaroczynski
 */
public class StateNodeManipulator extends NodeManipulator {
	public StateNodeManipulator(CrucibleReviewListModel reviewListModel, DefaultMutableTreeNode root) {
		super(reviewListModel, root);
	}

	@Override
	public int getChildCount(Object parent) {

		if (parent == rootNode) {
			return getDistinctStates().size();
		} else if (parent instanceof CrucibleReviewStateTreeNode) {
			CrucibleReviewStateTreeNode stateNode = (CrucibleReviewStateTreeNode) parent;
			return gentNumOfReviewsInState(stateNode.getCrucibleState());
		}

		return 0;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == rootNode) {

			DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			State state = getDistinctStates().get(index);

			CrucibleReviewStateTreeNode stateNode = new CrucibleReviewStateTreeNode(state);
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

		return null;

	}

	private List<State> getDistinctStates() {
		Set<State> states = new TreeSet<State>(COMPARATOR);

		for (ReviewAdapter review : reviewListModel.getReviews()) {
			states.add(review.getState());
		}

		return new ArrayList<State>(states);
	}

	private static final Comparator<State> COMPARATOR = new Comparator<State>() {
		public int compare(State lhs, State rhs) {
			return lhs.ordinal() - rhs.ordinal();
		}
	};

	private int gentNumOfReviewsInState(State crucibleState) {
		int ret = 0;
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getState() == crucibleState) {
				++ret;
			}
		}

		return ret;
	}	

	private ReviewAdapter getReviewInState(State crucibleState, int index) {
		List<ReviewAdapter> array = new ArrayList<ReviewAdapter>();

		// get all reviews in state
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getState() == crucibleState) {
				array.add(review);
			}
		}

		return array.get(index);
	}
}
