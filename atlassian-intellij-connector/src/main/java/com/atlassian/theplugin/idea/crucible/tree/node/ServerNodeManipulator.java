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
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jacek Jaroczynski
 */
public class ServerNodeManipulator extends NodeManipulator {
    private final ProjectCfgManagerImpl cfgManager;

    public ServerNodeManipulator(ProjectCfgManagerImpl cfgManager, CrucibleReviewListModel reviewListModel,
                                 DefaultMutableTreeNode root) {
		super(reviewListModel, root);
        this.cfgManager = cfgManager;
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

			ServerData server = getDistinctServers().get(index);

			CrucibleReviewServerTreeNode serverNode = new CrucibleReviewServerTreeNode(cfgManager, server);
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

	private List<ServerData> getDistinctServers() {
		Map<String, ServerData> servers = new TreeMap<String, ServerData>();

		for (ReviewAdapter review : reviewListModel.getReviews()) {

            ServerData server = cfgManager.getServerr(review.getServerData().getServerId());
            if (server != null) {
                String serverName = server.getName();
                servers.put(serverName, review.getServerData());
            }
		}

		return new ArrayList<ServerData>(servers.values());
	}

//	private static final Comparator<ServerCfg> COMPARATOR = new Comparator<ServerCfg>() {
//		public int compare(ServerCfg lhs, ServerCfg rhs) {
//			return lhs.getName().compareTo(rhs.getName());
//		}
//	};

	private int gentNumOfReviewsForServer(ServerData server) {
		int ret = 0;
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (server.equals(review.getServerData())) {
				++ret;
			}
		}

		return ret;
	}

	private ReviewAdapter getReviewForServer(ServerData server, int index) {
		List<ReviewAdapter> array = new ArrayList<ReviewAdapter>();

		// get all reviews in state
		for (ReviewAdapter review : reviewListModel.getReviews()) {
			if (server.equals(review.getServerData())) {
				array.add(review);
			}
		}

		return array.get(index);
	}

}
