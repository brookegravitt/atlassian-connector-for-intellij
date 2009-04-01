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
import com.atlassian.theplugin.idea.bamboo.BuildListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTreeModel extends DefaultTreeModel {

	private BuildListModel buildListModel;

	private BuildGroupBy groupBy = BuildGroupBy.NONE;

	private BuildNodeManipulator generalNodeManipulator;
	private BuildNodeManipulator stateNodeManipulator;
	private BuildNodeManipulator serverNodeManipulator;
	private BuildNodeManipulator dateNodeManipulator;
	private BuildNodeManipulator projectNodeManipulator;

	public BuildTreeModel(final BuildListModel buildListModel) {
		super(new DefaultMutableTreeNode());

		this.buildListModel = buildListModel;

		generalNodeManipulator = new GeneralBuildNodeManipulator(buildListModel, getRoot());
		stateNodeManipulator = new StateBuildNodeManipulator(buildListModel, getRoot());
		serverNodeManipulator = new ServerBuildNodeManipulator(buildListModel, getRoot());
		dateNodeManipulator = new DateBuildNodeManipulator(buildListModel, getRoot());
		projectNodeManipulator = new ProjectBuildNodeManipulator(buildListModel, getRoot());

	}

	/**
	 * Sets groupBy field used to group the tree and triggers tree to rebuild
	 * Only tree should use that method.
	 * @param aGroupBy group by option
	 */
	public void groupBy(BuildGroupBy aGroupBy) {
		setGroupBy(aGroupBy);

		// clear entire tree
		getRoot().removeAllChildren();

		// redraw tree
		nodeStructureChanged(getRoot());
	}

	/**
	 * Simple setter (does not trigger tree to rebuild)
	 * Used when initializig tree (before first load of builds status)
	 * @param groupBy group by option
	 */
	public void setGroupBy(BuildGroupBy groupBy) {
		if (groupBy != null) {
			this.groupBy = groupBy;
		}
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
			case DATE:
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
			case DATE:
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
				|| node instanceof BuildDateTreeNode
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
				return new ArrayList<BambooBuildAdapterIdea>(buildListModel.getBuilds()).indexOf(build);
			}
		}

		return -1;
	}

//	public void update(final Collection<BambooBuildAdapterIdea> buildStatuses) {
//		buildListModel.setBuilds(buildStatuses);
//		update();
//	}

	public void update() {
		getRoot().removeAllChildren();
		nodeStructureChanged(getRoot());
	}

	public BuildListModel getBuildListModel() {
		return buildListModel;
	}
}