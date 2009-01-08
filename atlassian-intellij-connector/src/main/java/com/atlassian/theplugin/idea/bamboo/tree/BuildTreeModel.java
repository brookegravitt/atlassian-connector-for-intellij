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

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.bamboo.BuildGroupBy;
import com.atlassian.theplugin.idea.bamboo.BuildModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTreeModel extends DefaultTreeModel {

	private BuildModel buildModel = new BuildModel();

	private BuildGroupBy groupBy = BuildGroupBy.STATE;

	private BuildNodeManipulator generalNodeManipulator;
	private BuildNodeManipulator stateNodeManipulator;
	private BuildNodeManipulator serverNodeManipulator;
	private BuildNodeManipulator dateNodeManipulator;
	private BuildNodeManipulator projectNodeManipulator;

	public BuildTreeModel() {
		super(new DefaultMutableTreeNode());

		// todo add group by node manipulators
		generalNodeManipulator = new GeneralBuildNodeManipulator(buildModel, getRoot());
		stateNodeManipulator = new StateBuildNodeManipulator(buildModel, getRoot());
		serverNodeManipulator = new ServerBuildNodeManipulator(buildModel, getRoot());
		dateNodeManipulator = new DateBuildNodeManipulator(buildModel, getRoot());
		projectNodeManipulator = new ProjectBuildNodeManipulator(buildModel, getRoot());
	}

	/**
	 * Sets groupBy field used to group the tree and triggers tree to rebuild
	 * Only tree should use that method.
	 * @param aGroupBy
	 */
	public void groupBy(BuildGroupBy aGroupBy) {
		this.groupBy = aGroupBy;

		// clear entire tree
		getRoot().removeAllChildren();

		// redraw tree
		nodeStructureChanged(getRoot());
	}

	/**
	 * Simple setter (does not trigger tree to rebuild)
	 * @param groupBy
	 */
	public void setGroupBy(BuildGroupBy groupBy) {
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
				return dateNodeManipulator.getChild(parent, index);
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
				return dateNodeManipulator.getChildCount(parent);
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
				|| node instanceof BuildProjectTreeNode
				|| node instanceof BuildStateTreeNode
				|| node instanceof BuildServerTreeNode) {
			return false;
		}

		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("valueForPathChanged");
	}

	@Override
	// todo add group by handling if necessary
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == getRoot()) {
			if (child instanceof BuildTreeNode) {
				BambooBuildAdapterIdea build = ((BuildTreeNode) child).getBuild();
				return new ArrayList<BambooBuildAdapterIdea>(buildModel.getBuilds()).indexOf(build);
			}
		}

		return -1;
	}

	public void update(final Collection<BambooBuildAdapterIdea> buildStatuses) {
		buildModel.setBuils(buildStatuses);
		getRoot().removeAllChildren();
		nodeStructureChanged(getRoot());
		nodeChanged(getRoot());
		nodeStructureChanged(root);
		nodeChanged(root);
	}
}
