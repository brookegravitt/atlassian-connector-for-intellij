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
import com.atlassian.theplugin.idea.bamboo.BambooFilterListTestUi;
import com.atlassian.theplugin.idea.bamboo.BuildGroupBy;
import com.atlassian.theplugin.idea.bamboo.BuildListModelImpl;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;
import javax.swing.JScrollPane;
import java.util.Collection;

public final class BuildTreeTestUi {

	private BuildTreeTestUi() {
	}

	public static void main(String[] args) {
		BuildListModelImpl buildListModel = new BuildListModelImpl(null, null);
		final JScrollPane jScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		final BuildTreeModel treeModel = new BuildTreeModel(buildListModel);
		final BuildTree tree = new BuildTree(BuildGroupBy.SERVER, treeModel, jScrollPane);
//		jScrollPane.addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentResized(final ComponentEvent e) {
//				tree.setUI(null);
//				tree.setUI(ui);
////				tree.setCellRenderer(new TreeRenderer());
//			}
//		});
		/*tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeExpanded(final TreeExpansionEvent event) {
//				ui.setLastWidth(jScrollPane.getViewport().getWidth());
//				tree.setUI(null);
//				tree.setUI(ui);
//				ui.setLastWidth(jScrollPane.getViewport().getWidth());
//				tree.setUI(null);
//				tree.setUI(ui);
//				uiSetup.registerUI(tree);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
//						ui.setLastWidth(jScrollPane.getViewport().getWidth());
						tree.setUI(null);
						tree.setUI(ui);
					}
				});
			}

			public void treeCollapsed(final TreeExpansionEvent event) {
//				ui.setLastWidth(jScrollPane.getViewport().getWidth());
//				tree.setCellRenderer(null);
//				tree.setCellRenderer(TREE_RENDERER);
//				tree.setUI(null);
//				tree.setUI(ui);
//				ui.setLastWidth(jScrollPane.getViewport().getWidth());
//				tree.setUI(null);
//				tree.setUI(ui);
//				uiSetup.registerUI(tree);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
//						ui.setLastWidth(jScrollPane.getViewport().getWidth());
						tree.setUI(null);
						tree.setUI(ui);
					}
				});
			}
		});*/

		//uiSetup.initializeUI(tree, jScrollPane);
		jScrollPane.setViewportView(tree);
//		tree.setUI(ui);
		Collection<BambooBuildAdapterIdea> buildAdapters = BambooFilterListTestUi.getBuilds();
		buildListModel.update(buildAdapters, null);
		SwingAppRunner.run(jScrollPane);
	}
}
