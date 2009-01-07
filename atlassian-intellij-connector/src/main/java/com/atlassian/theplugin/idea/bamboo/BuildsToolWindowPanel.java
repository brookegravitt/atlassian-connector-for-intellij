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
package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.bamboo.BuildDetailsInfo;
import com.atlassian.theplugin.configuration.BambooProjectConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTree;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeModel;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 * @author Jacek Jaroczynski
 */
public class BuildsToolWindowPanel extends PluginToolWindowPanel implements DataProvider {

	public static final String PLACE_PREFIX = BuildsToolWindowPanel.class.getSimpleName();
	private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();

	private final BambooProjectConfiguration bambooProjectConfiguration;
	private BuildTree buildTree;

	private BuildGroupBy groupBy = BuildGroupBy.NONE;
	private final ProjectCfgManager projectCfgManager;
	private final UiTaskExecutor uiTaskExecutor;
//	private final CrucibleReviewListModel buildModel;

	public BuildsToolWindowPanel(@NotNull final Project project, @NotNull final ProjectConfigurationBean projectConfiguration,
								  @NotNull final ProjectCfgManager projectCfgManager,
								 // @NotNull final CrucibleReviewListModel buildModel,
								  @NotNull final UiTaskExecutor uiTaskExecutor) {
		super(project, "ThePlugin.Builds.LeftToolBar", "ThePlugin.Builds.RightToolBar");

		this.projectCfgManager = projectCfgManager;
		this.uiTaskExecutor = uiTaskExecutor;

		bambooProjectConfiguration = projectConfiguration.getBambooConfiguration();

//		filterListModel = new CrucibleFilterListModel(
//				bambooProjectConfiguration.getCrucibleFilters().getManualFilter());
//		filterTreeModel = new CrucibleFilterTreeModel(filterListModel, buildModel);
//		this.buildModel = buildModel;
//		CrucibleReviewListModel sortingListModel = new SortingByKeyCrucibleReviewListModel(this.buildModel);
//		searchingReviewListModel = new SearchingCrucibleReviewListModel(sortingListModel);

		init();

//		filterTree.addSelectionListener(new LocalBuildFilterListModelListener());
	}

	@Override
	public void init() {
		super.init();

		addBuildTreeListeners();
		setupBuildTree();

		initToolBar();

		addSearchBoxListener();		
	}

	private void setupBuildTree() {
		TreeUISetup uiSetup = new TreeUISetup(TREE_RENDERER);
		uiSetup.initializeUI(buildTree, getRightScrollPane());
	}

	private void initToolBar() {
		// restore GroupBy setting
//		if (bambooProjectConfiguration.getView() != null && bambooProjectConfiguration.getView().getGroupBy() != null) {
//			groupBy = bambooProjectConfiguration.getView().getGroupBy();
//		}
//		buildTree.setGroupingType(groupBy);
	}

	private void addBuildTreeListeners() {
		buildTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				final BuildDetailsInfo buildDetailsInfo = buildTree.getSelectedBuild();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && buildDetailsInfo != null) {
					openBuild(buildDetailsInfo);
				}
			}
		});

		buildTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final BuildDetailsInfo buildDetailsInfo = buildTree.getSelectedBuild();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && buildDetailsInfo != null) {
					openBuild(buildDetailsInfo);
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = buildTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = buildTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
					buildTree.setSelectionPath(selPath);
					final BuildDetailsInfo buildDetailsInfo = buildTree.getSelectedBuild();
					if (buildDetailsInfo != null) {
						launchContextMenu(e);
					}
				}
			}
		});
	}

	private void openBuild(final BuildDetailsInfo buildDetailsInfo) {

	}

	private void launchContextMenu(MouseEvent e) {
//		final DefaultActionGroup actionGroup = new DefaultActionGroup();
//
//		final ActionGroup configActionGroup = (ActionGroup) ActionManager
//				.getInstance().getAction("ThePlugin.Reviews.ReviewPopupMenu");
//		actionGroup.addAll(configActionGroup);
//
//		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(getActionPlaceName(), actionGroup);
//
//		final JPopupMenu jPopupMenu = popup.getComponent();
//		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void setPanelEnabled(boolean enabled) {
		if (buildTree != null) {
			buildTree.setEnabled(enabled);
		}
//		if (filterTree != null) {
//			filterTree.setEnabled(enabled);
//		}
		if (getSearchField() != null) {
			getSearchField().setEnabled(enabled);
		}
		if (getStatusBarPane() != null) {
			getStatusBarPane().setEnabled(enabled);
		}
//		filterTree.redrawNodes();
	}

	@Nullable
	public Object getData(@NonNls String dataId) {
//		if (dataId.equals(Constants.REVIEW)) {
//			return buildTree.getSelectedBuild();
//		} else if (dataId.equals(Constants.REVIEW_WINDOW_ENABLED)) {
//			return buildTree.isEnabled();
//		}
		return null;
	}

	@Override
	public void addSearchBoxListener() {
		getSearchField().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
//				searchingReviewListModel.setSearchTerm(getSearchField().getText());
			}

			public void removeUpdate(DocumentEvent e) {
//				searchingReviewListModel.setSearchTerm(getSearchField().getText());
			}

			public void changedUpdate(DocumentEvent e) {
//				searchingReviewListModel.setSearchTerm(getSearchField().getText());
			}
		});
		
		getSearchField().addKeyboardListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					getSearchField().addCurrentTextToHistory();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});
	}

	@Override
	public JTree createRightTree() {
		if (buildTree == null) {
			buildTree = new BuildTree(new BuildTreeModel());

		}
		return buildTree;
	}

	@Override
	public JTree createLeftTree() {
//		if (filterTree == null && filterTreeModel != null) {
//			filterTree = new FilterTree(filterTreeModel, bambooProjectConfiguration);
//		}
//
//		return filterTree;
		return new JTree();
	}


	@Override
	public String getActionPlaceName() {
		return PLACE_PREFIX + this.getProject().getName();
	}


	public void setGroupBy(BuildGroupBy groupBy) {
//		this.groupBy = groupBy;
//		buildTree.groupBy(groupBy);
////		expandAllRightTreeNodes();
//
//		bambooProjectConfiguration.getView().setGroupingType(groupBy);
	}

	public BuildGroupBy getGroupBy() {
		return groupBy;
	}

	public void refresh() {
//		Task.Backgroundable refresh = new Task.Backgroundable(getProject(), "Refreshing Crucible Panel", false) {
//			@Override
//			public void run(@NotNull final ProgressIndicator indicator) {
//					buildModel.rebuildModel(reason);
//			}
//		};
//		ProgressManager.getInstance().run(refresh);
	}

	public void expandRightTree() {

	}

	public void collapseRightTree() {
		
	}

//	public BambooStatusListener getStatusCheckerListener() {
//		return buildTree;
//	}

	private class LocalBuildFilterListModelListener /*implements BuildFilterSelectionListener*/ {

	}

}