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

import com.atlassian.theplugin.commons.bamboo.BuildDetailsInfo;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.bamboo.BuildGroupBy;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.AbstractTree;
import com.intellij.openapi.project.Project;

import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTree extends AbstractTree {
	private BuildTreeModel buildTreeModel;

	public BuildTree(final Project project, final BuildGroupBy groupBy, final BuildTreeModel buildTreeModel) {
		super(buildTreeModel);

		this.buildTreeModel = buildTreeModel;
		this.buildTreeModel.setGroupBy(groupBy);

		init();

		addMouseListener(new PopupAwareMouseAdapter() {
			protected void onPopup(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && getSelectionModel().getSelectionCount() == 1) {
					Object o = getSelectionPath().getLastPathComponent();
					if (o instanceof BuildTreeNode) {
						IdeaHelper.getBuildToolWindow(project).showBuild(((BuildTreeNode) o).getBuild());
					}
				}
			}
		});
	}

	private void init() {
		setRootVisible(false);
		setShowsRootHandles(true);
		setExpandsSelectedPaths(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	public BuildDetailsInfo getSelectedBuild() {
		return null;
	}

	public void updateModel(final Collection<BambooBuildAdapterIdea> buildStatuses) {
		buildTreeModel.update(buildStatuses);
		expandTree();
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

	public void resetState() {
	}

	public void groupBy(final BuildGroupBy groupingType) {
		buildTreeModel.groupBy(groupingType);
		expandTree();
	}
}
