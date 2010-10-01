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
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * @author Jacek Jaroczynski
 */
public class AuthorNodeManipulator extends NodeManipulator {
	public AuthorNodeManipulator(CrucibleReviewListModel reviewListModel, DefaultMutableTreeNode root) {
		super(reviewListModel, root);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return getDistinctAuthors().size();
		} else if (parent instanceof CrucibleReviewAuthorTreeNode) {
			CrucibleReviewAuthorTreeNode authorNode = (CrucibleReviewAuthorTreeNode) parent;
			return gentNumOfReviewsForAuthor(authorNode.getAuthor());
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

			User author = getDistinctAuthors().get(index);

			CrucibleReviewAuthorTreeNode serverNode = new CrucibleReviewAuthorTreeNode(author);
			p.add(serverNode);

			return serverNode;

		} else if (parent instanceof CrucibleReviewAuthorTreeNode) {
			CrucibleReviewAuthorTreeNode p = (CrucibleReviewAuthorTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			ReviewAdapter review = getReviewForAuthor(p.getAuthor(), index);
			CrucibleReviewTreeNode node = new CrucibleReviewTreeNode(reviewListModel, review);
			p.add(node);

			return node;
		}

		return null;

	}

	private List<User> getDistinctAuthors() {
		Set<User> servers = new TreeSet<User>(COMPARATOR);

		for (ReviewAdapter review : reviewListModel.getReviews()) {
			servers.add(review.getAuthor());
		}

		return new ArrayList<User>(servers);
	}

	private static final Comparator<User> COMPARATOR = new Comparator<User>() {
		public int compare(User lhs, User rhs) {
			return lhs.getDisplayName().compareTo(rhs.getDisplayName());
		}
	};


	private int gentNumOfReviewsForAuthor(User author) {
		int ret = 0;
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getAuthor().equals(author)) {
				++ret;
			}
		}

		return ret;
	}

	private ReviewAdapter getReviewForAuthor(User author, int index) {
		List<ReviewAdapter> array = new ArrayList<ReviewAdapter>();

		// get all reviews in state
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getAuthor().equals(author)) {
				array.add(review);
			}
		}

		return array.get(index);
	}

}
