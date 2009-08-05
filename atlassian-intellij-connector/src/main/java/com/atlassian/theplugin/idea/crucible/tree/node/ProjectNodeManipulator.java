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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

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
			return gentNumOfReviewsForProject(serverNode.getProject().getKey());
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

			CrucibleProject crucibleProject = getDistinctProjects().get(index);

			CrucibleReviewProjectTreeNode serverNode = new CrucibleReviewProjectTreeNode(crucibleProject);
			p.add(serverNode);

			return serverNode;

		} else if (parent instanceof CrucibleReviewProjectTreeNode) {
			CrucibleReviewProjectTreeNode p = (CrucibleReviewProjectTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			ReviewAdapter review = getReviewForProject(p.getProject().getKey(), index);
			CrucibleReviewTreeNode node = new CrucibleReviewTreeNode(reviewListModel, review);
			p.add(node);

			return node;
		}

		return null;
	}

	private List<CrucibleProject> getDistinctProjects() {
		Set<CrucibleProject> projects = new TreeSet<CrucibleProject>(COMPARATOR);

		for (ReviewAdapter review : reviewListModel.getReviews()) {
			projects.add(review.getCrucibleProject());
		}

		return new ArrayList<CrucibleProject>(projects);
	}

	private static final Comparator<CrucibleProject> COMPARATOR = new Comparator<CrucibleProject>() {
		public int compare(CrucibleProject lhs, CrucibleProject rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
	};

	private int gentNumOfReviewsForProject(String projectKey) {
		int ret = 0;
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getProjectKey().equals(projectKey)) {
				++ret;
			}
		}

		return ret;
	}

	private ReviewAdapter getReviewForProject(String projectKey, int index) {
		List<ReviewAdapter> array = new ArrayList<ReviewAdapter>();

		// get all reviews in state
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (review.getProjectKey().equals(projectKey)) {
				array.add(review);
			}
		}

		return array.get(index);
	}
}
