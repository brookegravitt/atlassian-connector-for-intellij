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

import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * @author Jacek Jaroczynski
 */
public class ServerNodeManipulator extends NodeManipulator {
	private final ProjectCfgManagerImpl projectCfgManager;

	public ServerNodeManipulator(CrucibleReviewListModel reviewListModel, DefaultMutableTreeNode root,
			@NotNull ProjectCfgManagerImpl projectCfgManager) {
		super(reviewListModel, root);
		this.projectCfgManager = projectCfgManager;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return getDistinctServers().size();
		} else if (parent instanceof CrucibleReviewServerTreeNode) {
			CrucibleReviewServerTreeNode serverNode = (CrucibleReviewServerTreeNode) parent;
			return gentNumOfReviewsForServer(serverNode.getCrucibleServer());
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

			ServerCfg server = getDistinctServers().get(index);

			CrucibleReviewServerTreeNode serverNode = new CrucibleReviewServerTreeNode(server);
			p.add(serverNode);

			return serverNode;

		} else if (parent instanceof CrucibleReviewServerTreeNode) {
			CrucibleReviewServerTreeNode p = (CrucibleReviewServerTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			ReviewAdapter review = getReviewForServer(p.getCrucibleServer(), index);
			CrucibleReviewTreeNode node = new CrucibleReviewTreeNode(reviewListModel, review);
			p.add(node);

			return node;
		}

		return null;

	}

	private List<ServerCfg> getDistinctServers() {
		Set<ServerCfg> servers = new TreeSet<ServerCfg>(COMPARATOR);

		for (ReviewAdapter review : reviewListModel.getReviews()) {
			servers.add(projectCfgManager.getServer(review.getServerData()));
		}

		return new ArrayList<ServerCfg>(servers);
	}

	private static final Comparator<ServerCfg> COMPARATOR = new Comparator<ServerCfg>() {
		public int compare(ServerCfg lhs, ServerCfg rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
	};

	private int gentNumOfReviewsForServer(ServerCfg server) {
		int ret = 0;
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (server.equals(projectCfgManager.getServer(review.getServerData()))) {
				++ret;
			}
		}

		return ret;
	}

	private ReviewAdapter getReviewForServer(ServerCfg server, int index) {
		List<ReviewAdapter> array = new ArrayList<ReviewAdapter>();

		// get all reviews in state
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (server.equals(projectCfgManager.getServer(review.getServerData()))) {
				array.add(review);
			}
		}

		return array.get(index);
	}

}
