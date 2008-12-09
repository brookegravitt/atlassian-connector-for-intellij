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
package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModelImpl;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModelListener;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleFilterTreeModel;
import com.atlassian.theplugin.idea.crucible.tree.FilterTree;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTree;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTreeModel;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewsToolWindowPanel extends PluginToolWindowPanel implements DataProvider {

	public static final String PLACE_PREFIX = ReviewsToolWindowPanel.class.getSimpleName();
	private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();

	private final CrucibleReviewListModel reviewListModel;
	private CrucibleProjectConfiguration crucibleProjectConfiguration;
	private ReviewTree reviewTree;
	private ReviewTreeModel reviewTreeModel;
	private CrucibleFilterListModel filterListModel = new CrucibleFilterListModelImpl();
	private CrucibleFilterTreeModel filterTreeModel;

	private CrucibleReviewGroupBy groupBy = CrucibleReviewGroupBy.NONE;
	private FilterTree filterTree;
	private CrucibleCustomFilterDetailsPanel detailsPanel;


	public ReviewsToolWindowPanel(@NotNull final Project project,
			@NotNull final ProjectConfigurationBean projectConfiguration,
			@NotNull final CfgManager cfgManager,
			@NotNull final CrucibleReviewListModel reviewListModel) {
		super(project, cfgManager, "ThePlugin.Reviews.LeftToolBar", "ThePlugin.Reviews.RightToolBar");

		this.reviewListModel = reviewListModel;
		filterListModel = new CrucibleFilterListModelImpl();
		crucibleProjectConfiguration = projectConfiguration.getCrucibleConfiguration();
		filterListModel.setCustomFilter(crucibleProjectConfiguration.getCrucibleFilters().getManualFilter());
		filterTreeModel = new CrucibleFilterTreeModel(filterListModel);

		init();

		filterListModel.addListener(new LocalCrucibleFilterListModelLisener());
	}

	@Override
	public void init() {
		super.init();

		addReviewTreeListeners();
		setupReviewTree();

		initToolBar();

		filterListModel.addListener(new CrucibleFilterListModelListener() {

			public void filterChanged() {

			}

			public void selectedCustomFilter(CustomFilter customFilter) {
				showManualFilterPanel(true);
			}

			public void selectedPredefinedFilter(PredefinedFilter selectedPredefinedFilter) {
				showManualFilterPanel(false);
			}

		});


		detailsPanel = new CrucibleCustomFilterDetailsPanel(getProject(), getCfgManager(),
															crucibleProjectConfiguration,
															filterListModel);

		filterTreeModel.nodeChanged((DefaultMutableTreeNode)filterTreeModel.getRoot());


		if (crucibleProjectConfiguration.getView() != null && crucibleProjectConfiguration.getView().getGroupBy() != null) {
			groupBy = crucibleProjectConfiguration.getView().getGroupBy();
		}
		reviewTreeModel.setGroupBy(groupBy);
	}

	private void setupReviewTree() {
		TreeUISetup uiSetup = new TreeUISetup(TREE_RENDERER);
		uiSetup.initializeUI(reviewTree, getRightScrollPane());
	}

	private void initToolBar() {
		// restore GroupBy setting
		if (crucibleProjectConfiguration.getView() != null && crucibleProjectConfiguration.getView().getGroupBy() != null) {
			groupBy = crucibleProjectConfiguration.getView().getGroupBy();
		}
		reviewTreeModel.setGroupBy(groupBy);
	}

	public void openReview(final ReviewAdapter review) {
		CrucibleReviewWindow.getInstance(getProject()).showCrucibleReviewWindow(review);
	}

	private void addReviewTreeListeners() {
		reviewTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				final ReviewAdapter review = reviewListModel.getSelectedReview();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && review != null) {
					openReview(review);
				}
			}
		});

		reviewTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final ReviewAdapter review = reviewListModel.getSelectedReview();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && review != null) {
					openReview(review);
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = reviewTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = reviewTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
					reviewTree.setSelectionPath(selPath);
					final ReviewAdapter review = reviewListModel.getSelectedReview();
					if (review != null) {
						launchContextMenu(e);
					}
				}
			}
		});
	}

	private void launchContextMenu(MouseEvent e) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();

		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.Reviews.ReviewPopupMenu");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(getActionPlaceName(), actionGroup);

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	@Nullable
	public Object getData(@NonNls String dataId) {
		if (dataId.equals(Constants.REVIEW)) {
			return reviewListModel.getSelectedReview();
		}
		return null;

	}

	public void addSearchBoxListener() {

	}

	public JTree createRightTree() {
		if (reviewTree == null) {
			reviewTreeModel = new ReviewTreeModel(reviewListModel);
			reviewTree = new ReviewTree(reviewTreeModel);
			reviewTree.setRootVisible(false);
		}
		return reviewTree;
	}

	public JTree createLeftTree() {
		if (filterTree == null && filterTreeModel != null) {
			filterTree = new FilterTree(filterTreeModel, crucibleProjectConfiguration);
		}

		return filterTree;
	}

	public void onEditButtonClickAction() {
	}

	public String getActionPlaceName() {
		return PLACE_PREFIX + this.getProject().getName();
	}


	public void setGroupBy(CrucibleReviewGroupBy groupBy) {
		this.groupBy = groupBy;
		reviewTreeModel.groupBy(groupBy);
		expandAllRightTreeNodes();

		crucibleProjectConfiguration.getView().setGroupBy(groupBy);
	}

	public CrucibleReviewGroupBy getGroupBy() {
		return groupBy;
	}

	protected void showManualFilterPanel(boolean visible) {
		getSplitLeftPane().setOrientation(true);

		if (visible) {
			getSplitLeftPane().setSecondComponent(detailsPanel);
			getSplitLeftPane().setProportion(MANUAL_FILTER_PROPORTION_VISIBLE);

		} else {
			getSplitLeftPane().setSecondComponent(null);
			getSplitLeftPane().setProportion(MANUAL_FILTER_PROPORTION_HIDDEN);
		}
	}

	public void refresh() {
		final CrucibleStatusChecker checker = IdeaHelper.getCrucibleStatusChecker(getProject());

		if (checker != null) {
			if (checker.canSchedule()) {
				Task.Backgroundable refresh = new Task.Backgroundable(getProject(), "Refreshing Crucible Panel", false) {
							public void run(final ProgressIndicator indicator) {
								checker.newTimerTask().run();
							}
						};
				ProgressManager.getInstance().run(refresh);
			}
		}
	}

	private class LocalCrucibleFilterListModelLisener implements CrucibleFilterListModelListener {
		public void filterChanged() {

		}

		public void selectedCustomFilter(CustomFilter customFilter) {

		}


		public void selectedPredefinedFilter(PredefinedFilter selectedPredefinedFilter) {
			// clear all predefined filters from configuration (single selection support temporarily)
			Boolean[] confFilters = crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters();

			for (int i = 0; i < confFilters.length; ++i) {
				confFilters[i] = false;
			}

			// rember the filters selection in plugin configuration
			confFilters[selectedPredefinedFilter.ordinal()] = true;

			// restart checker
			refresh();
		}

	}
}
