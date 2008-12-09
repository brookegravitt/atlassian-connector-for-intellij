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

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public class ProjectNodeManipulator extends NodeManipulator {
	public ProjectNodeManipulator(CrucibleReviewListModel reviewListModel, DefaultMutableTreeNode root) {
		super(reviewListModel, root);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return getDistinctProjects().size();
		} else if (parent instanceof CrucibleReviewProjectTreeNode) {
			CrucibleReviewProjectTreeNode serverNode = (CrucibleReviewProjectTreeNode) parent;
			return gentNumOfReviewsForProject(serverNode.getProject());
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

			String project = getDistinctProjects().get(index);

			CrucibleReviewProjectTreeNode serverNode = new CrucibleReviewProjectTreeNode(project);
			p.add(serverNode);

			return serverNode;

		} else if (parent instanceof CrucibleReviewProjectTreeNode) {
			CrucibleReviewProjectTreeNode p = (CrucibleReviewProjectTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			ReviewAdapter review = getReviewForProject(p.getProject(), index);
			CrucibleReviewTreeNode node = new CrucibleReviewTreeNode(review);
			p.add(node);

			return node;
		}

		return null;
	}

	private List<String> getDistinctProjects() {
		Set<String> servers = new LinkedHashSet<String>();	// ordered set

		for (ReviewAdapter review : reviewListModel.getReviews()) {
			servers.add(review.getProjectKey());
		}

		return new ArrayList<String>(servers);
	}

	private int gentNumOfReviewsForProject(String project) {
		int ret = 0;
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getProjectKey().equals(project)) {
				++ret;
			}
		}

		return ret;
	}

	private ReviewAdapter getReviewForProject(String project, int index) {
		List<ReviewAdapter> array = new ArrayList<ReviewAdapter>();

		// get all reviews in state
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getProjectKey().equals(project)) {
				array.add(review);
			}
		}

		return array.get(index);
	}
}
