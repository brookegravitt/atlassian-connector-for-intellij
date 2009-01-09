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
import com.atlassian.theplugin.idea.ui.tree.AbstractTree;
import com.intellij.openapi.project.Project;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.Collection;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTree extends AbstractTree {
	private BuildTreeModel buildTreeModel;

	private TreeModelListener localTreeModelListener = new LocalTreeModelListener();

	public BuildTree(final Project project, final BuildGroupBy groupBy, final BuildTreeModel buildTreeModel) {
		super(buildTreeModel);

		this.buildTreeModel = buildTreeModel;
		this.buildTreeModel.setGroupBy(groupBy);

		init();

		buildTreeModel.addTreeModelListener(localTreeModelListener);
	}

	private void init() {
		setRootVisible(false);
		setShowsRootHandles(true);
		setExpandsSelectedPaths(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	public BambooBuildAdapterIdea getSelectedBuild() {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null && selectionPath.getLastPathComponent() != null
				&& selectionPath.getLastPathComponent() instanceof BuildTreeNode) {
			return ((BuildTreeNode) selectionPath.getLastPathComponent()).getBuild();
		} else {
			// nothing selected
			return null;
		}
	}

	private void selectBuildNode(BambooBuildAdapterIdea build) {
		if (build == null) {
			clearSelection();
			return;
		}

		for (int i = 0; i < getRowCount(); i++) {
			TreePath path = getPathForRow(i);
			Object object = path.getLastPathComponent();
			if (object instanceof BuildTreeNode) {
				BuildTreeNode node = (BuildTreeNode) object;
				if (node.getBuild().getBuildKey().equals(build.getBuildKey())) {
					expandPath(path);
					makeVisible(path);
					setSelectionPath(path);
					break;
				}
			}
		}
	}

	public void updateModel(final Collection<BambooBuildAdapterIdea> buildStatuses) {

		Set<TreePath> collapsedPaths = getCollapsedPaths();
		BambooBuildAdapterIdea build = getSelectedBuild();

		// rebuild the tree
		buildTreeModel.update(buildStatuses);

		// expand entire tree
		expandTree();

		// restore selection and collapse state
		collapsePaths(collapsedPaths);
		selectBuildNode(build);	
	}

//	public void updateBuildStatuses(final Collection<BambooBuild> buildStatuses) {
//		final Collection<BambooBuildAdapterIdea> collection = new ArrayList<BambooBuildAdapterIdea>();
//		for (BambooBuild build : buildStatuses) {
//			BambooBuildAdapterIdea buildAdapter = new BambooBuildAdapterIdea(build);
//			collection.add(buildAdapter);
//		}
//
//		updateModel(collection);
//	}

	public void groupBy(final BuildGroupBy groupingType) {
		buildTreeModel.groupBy(groupingType);
		expandTree();
	}

	private class LocalTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(final TreeModelEvent e) {

		}

		public void treeNodesInserted(final TreeModelEvent e) {

		}

		public void treeNodesRemoved(final TreeModelEvent e) {

		}

		public void treeStructureChanged(final TreeModelEvent e) {
			expandTree();
		}
	}
}
