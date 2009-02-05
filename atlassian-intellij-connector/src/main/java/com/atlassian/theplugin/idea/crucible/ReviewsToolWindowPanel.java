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

import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListener;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListenerAdapter;
import com.atlassian.theplugin.crucible.model.SearchingCrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.SortingByKeyCrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.UpdateContext;
import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.filters.CustomFilterChangeListener;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleFilterTreeModel;
import com.atlassian.theplugin.idea.crucible.tree.FilterTree;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTree;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTreeModel;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewsToolWindowPanel extends PluginToolWindowPanel implements DataProvider {

	public static final String PLACE_PREFIX = ReviewsToolWindowPanel.class.getSimpleName();
	private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();

	private final CrucibleProjectConfiguration crucibleProjectConfiguration;
	private ReviewTree reviewTree;
	private CrucibleFilterListModel filterListModel;
	private CrucibleFilterTreeModel filterTreeModel;

	private CrucibleReviewGroupBy groupBy = CrucibleReviewGroupBy.NONE;
	private FilterTree filterTree;
	private CrucibleCustomFilterDetailsPanel detailsPanel;
	private SearchingCrucibleReviewListModel searchingReviewListModel;
	private final ProjectCfgManager projectCfgManager;
	private final UiTaskExecutor uiTaskExecutor;
	private final CrucibleReviewListModel reviewListModel;
	private Timer timer;

	private static final int ONE_SECOND = 1000;

	public ReviewsToolWindowPanel(@NotNull final Project project, @NotNull final ProjectConfigurationBean projectConfiguration,
			@NotNull final ProjectCfgManager projectCfgManager,
			@NotNull final CrucibleReviewListModel reviewListModel,
			@NotNull final UiTaskExecutor uiTaskExecutor) {
		super(project, "ThePlugin.Reviews.LeftToolBar", "ThePlugin.Reviews.RightToolBar");
		this.projectCfgManager = projectCfgManager;
		this.uiTaskExecutor = uiTaskExecutor;

		crucibleProjectConfiguration = projectConfiguration.getCrucibleConfiguration();

		filterListModel = new CrucibleFilterListModel(
				crucibleProjectConfiguration.getCrucibleFilters().getManualFilter());
		filterTreeModel = new CrucibleFilterTreeModel(filterListModel, reviewListModel);
		this.reviewListModel = reviewListModel;
		CrucibleReviewListModel sortingListModel = new SortingByKeyCrucibleReviewListModel(this.reviewListModel);
		searchingReviewListModel = new SearchingCrucibleReviewListModel(sortingListModel);
		init(Constants.DIALOG_MARGIN / 2);
		this.reviewListModel.addListener(new LocalCrucibleReviewListModelListener());
		filterTree.addSelectionListener(new LocalCrucibleFilterListModelListener());
	}

	@Override
	public void init(int margin) {
		super.init(margin);

		addReviewTreeListeners();
		setupReviewTree();

		initToolBar();

		detailsPanel = new CrucibleCustomFilterDetailsPanel(
				getProject(), projectCfgManager, crucibleProjectConfiguration,
				filterTree, CrucibleServerFacadeImpl.getInstance(), uiTaskExecutor);
		detailsPanel.addCustomFilterChangeListener(new CustomFilterChangeListener() {
			public void customFilterChanged(CustomFilter customFilter) {
				refresh(UpdateReason.FILTER_CHANGED);
			}
		});

		filterTreeModel.nodeChanged((DefaultMutableTreeNode) filterTreeModel.getRoot());

		if (crucibleProjectConfiguration != null
				&& crucibleProjectConfiguration.getCrucibleFilters().getManualFilter() != null
				&& crucibleProjectConfiguration.getCrucibleFilters().getManualFilter().isEnabled()) {
			showManualFilterPanel(true);
		} else {
			showManualFilterPanel(false);
		}

		addSearchBoxListener();
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
		reviewTree.setGroupBy(groupBy);
	}

	public void openReview(final ReviewAdapter review) {
		reviewListModel.getOpenInIdeReviews().clear();
		CommentHighlighter.removeCommentsInEditors(project);
		reviewListModel.getOpenInIdeReviews().add(review);
		IdeaHelper.getCrucibleToolWindow(getProject()).showReview(review);
	}

	private void addReviewTreeListeners() {
		reviewTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				final ReviewAdapter review = reviewTree.getSelectedReview();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && review != null) {
					openReview(review);
				}
			}
		});

		reviewTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final ReviewAdapter review = reviewTree.getSelectedReview();
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
					final ReviewAdapter review = reviewTree.getSelectedReview();
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

	private void setPanelEnabled(boolean enabled) {
		if (reviewTree != null) {
			reviewTree.setEnabled(enabled);
		}
		if (filterTree != null) {
			filterTree.setEnabled(enabled);
		}
		if (getSearchField() != null) {
			getSearchField().setEnabled(enabled);
		}
		if (getStatusBarPane() != null) {
			getStatusBarPane().setEnabled(enabled);
		}
		filterTree.redrawNodes();
	}

	@Nullable
	public Object getData(@NonNls String dataId) {
		if (reviewTree != null) {
			if (dataId.equals(Constants.REVIEW)) {
				return reviewTree.getSelectedReview();
			} else if (dataId.equals(Constants.REVIEW_WINDOW_ENABLED)) {
				return reviewTree.isEnabled();
			}
		}
		return null;
	}

	@Override
	protected void addSearchBoxListener() {
		getSearchField().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				triggerDelayedSearchBoxUpdate();
			}

			public void removeUpdate(DocumentEvent e) {
				triggerDelayedSearchBoxUpdate();
			}

			public void changedUpdate(DocumentEvent e) {
				triggerDelayedSearchBoxUpdate();
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

	private void triggerDelayedSearchBoxUpdate() {
		if (timer != null && timer.isRunning()) {
			return;
		}
		timer = new Timer(ONE_SECOND, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchingReviewListModel.setSearchTerm(getSearchField().getText());
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	@Override
	public JTree createRightTree() {
		if (reviewTree == null) {
			reviewTree = new ReviewTree(new ReviewTreeModel(searchingReviewListModel));

		}
		return reviewTree;
	}

	@Override
	public JTree createLeftTree() {
		if (filterTree == null && filterTreeModel != null) {
			filterTree = new FilterTree(filterTreeModel, crucibleProjectConfiguration);
		}

		return filterTree;
	}

	@Override
	public String getActionPlaceName() {
		return PLACE_PREFIX + this.getProject().getName();
	}


	public void setGroupBy(CrucibleReviewGroupBy groupBy) {
		this.groupBy = groupBy;
		reviewTree.groupBy(groupBy);

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

	public void refresh(final UpdateReason reason) {
		Task.Backgroundable refresh = new Task.Backgroundable(getProject(), "Refreshing Crucible Panel", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				reviewListModel.rebuildModel(reason);
			}
		};
		ProgressManager.getInstance().run(refresh);
	}

	private class LocalCrucibleFilterListModelListener implements CrucibleFilterSelectionListener {
		public void filterChanged() {

		}

		public void selectedCustomFilter(CustomFilter customFilter) {
			showManualFilterPanel(true);
		}

		public void selectedPredefinedFilters(Collection<PredefinedFilter> predefinedFilters) {
			Boolean[] confFilters = crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters();

			// clear all predefined filters from configuration
			for (int i = 0; i < confFilters.length; ++i) {
				confFilters[i] = false;
			}

			// rember the filters selection in plugin configuration
			for (PredefinedFilter filter : predefinedFilters) {
				confFilters[filter.ordinal()] = true;
			}

			// restart checker
			refresh(UpdateReason.FILTER_CHANGED);
		}

		public void unselectedCustomFilter() {
			showManualFilterPanel(false);
		}
	}

	private class LocalCrucibleReviewListModelListener extends CrucibleReviewListModelListenerAdapter {
		private Exception exception = null;

		@Override
		public void reviewListUpdateStarted(UpdateContext updateContext) {
			exception = null;
			if (updateContext.getUpdateReason() != UpdateReason.TIMER_FIRED) {
				setPanelEnabled(false);
				setStatusMessage("Loading reviews...");
			}
		}

		@Override
		public void reviewListUpdateFinished(UpdateContext updateContext) {
			setPanelEnabled(true);
			if (exception != null) {
				setStatusMessage(exception.getMessage(), true);
			} else {
				setStatusMessage("Loaded " + reviewListModel.getReviews().size() + " reviews");
			}
		}

		@Override
		public void reviewListUpdateError(final UpdateContext updateContext, final Exception e) {
			this.exception = e;
		}
	}
}
