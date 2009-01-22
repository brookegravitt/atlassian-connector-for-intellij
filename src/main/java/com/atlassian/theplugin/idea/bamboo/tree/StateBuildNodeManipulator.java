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
package com.atlassian.theplugin.idea.bamboo.tree;

import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.bamboo.BuildListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * @author Jacek Jaroczynski
 */
public class StateBuildNodeManipulator extends BuildNodeManipulator {
	public StateBuildNodeManipulator(final BuildListModel buildModel, final DefaultMutableTreeNode root) {
		super(buildModel, root);
	}
	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return getDistinctStates().size();
		} else if (parent instanceof BuildStateTreeNode) {
			BuildStateTreeNode stateNode = (BuildStateTreeNode) parent;
			return gentNumOfBuildsForState(stateNode.getStatus());
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

			BuildStatus state = getDistinctStates().get(index);

			BuildStateTreeNode stateNode = new BuildStateTreeNode(state);
			p.add(stateNode);

			return stateNode;

		} else if (parent instanceof BuildStateTreeNode) {
			BuildStateTreeNode p = (BuildStateTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			BambooBuildAdapterIdea build = getBuildForState(p.getStatus(), index);
			BuildTreeNode node = new BuildTreeNode(build);
			p.add(node);

			return node;
		}

		return null;
	}

	private List<BuildStatus> getDistinctStates() {
		Set<BuildStatus> states = new TreeSet<BuildStatus>(COMPARATOR);

		for (BambooBuildAdapterIdea build : buildModel.getBuilds()) {
			states.add(build.getStatus());
		}

		return new ArrayList<BuildStatus>(states);
	}

	private static final Comparator<BuildStatus> COMPARATOR = new Comparator<BuildStatus>() {
		public int compare(BuildStatus lhs, BuildStatus rhs) {
			return lhs.ordinal() - rhs.ordinal();
		}
	};

	private int gentNumOfBuildsForState(BuildStatus buildStatus) {
		int ret = 0;
		for (BambooBuildAdapterIdea build : buildModel.getBuilds()) {
			if (build.getStatus() == buildStatus) {
				++ret;
			}
		}

		return ret;
	}

	private BambooBuildAdapterIdea getBuildForState(BuildStatus buildStatus, int index) {
		List<BambooBuildAdapterIdea> array = new ArrayList<BambooBuildAdapterIdea>();

		// get all builds for server
		for (BambooBuildAdapterIdea build : buildModel.getBuilds()) {
			if (build.getStatus() == buildStatus) {
				array.add(build);
			}
		}

		return array.get(index);
	}
}
