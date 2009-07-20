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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.crucible.model.*;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.filters.CustomFilterChangeListener;
import com.atlassian.theplugin.idea.crucible.tree.*;
import com.atlassian.theplugin.idea.crucible.tree.node.CrucibleReviewTreeNode;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.TreeSpeedSearch;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewListToolWindowPanel extends PluginToolWindowPanel implements DataProvider {

	public static final String PLACE_PREFIX = ReviewListToolWindowPanel.class.getSimpleName();
	private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();
    private Collection<CustomFilterChangeListener> customFilterChangeListeners = new ArrayList<CustomFilterChangeListener>();

	private final CrucibleWorkspaceConfiguration crucibleProjectConfiguration;
	private ReviewTree reviewTree;
	private CrucibleFilterListModel filterListModel;
	private CrucibleFilterTreeModel filterTreeModel;

	private CrucibleReviewGroupBy groupBy = CrucibleReviewGroupBy.NONE;
	private FilterTree filterTree;
	private SearchingCrucibleReviewListModel searchingReviewListModel;
	private final ProjectCfgManagerImpl projectCfgManager;

        private final UiTaskExecutor uiTaskExecutor;
	private final CrucibleReviewListModel reviewListModel;
	private Timer timer;

	private static final int ONE_SECOND = 1000;
	private CrucibleReviewListModel currentReviewListModel;
    private static final String MANUAL_FILTER_MENU_PLACE = "crucible manual filter place";


    public ReviewListToolWindowPanel(@NotNull final Project project,
			@NotNull final WorkspaceConfigurationBean projectConfiguration,
			@NotNull final ProjectCfgManagerImpl projectCfgManager,
			@NotNull final CrucibleReviewListModel reviewListModel,
			@NotNull final UiTaskExecutor uiTaskExecutor) {
		super(project, "ThePlugin.Reviews.LeftToolBar", "ThePlugin.Reviews.RightToolBar");
		this.projectCfgManager = projectCfgManager;
		this.uiTaskExecutor = uiTaskExecutor;

		crucibleProjectConfiguration = projectConfiguration.getCrucibleConfiguration();

		filterListModel = new CrucibleFilterListModel(
				crucibleProjectConfiguration.getCrucibleFilters().getManualFilter(),
				crucibleProjectConfiguration.getCrucibleFilters().getRecenltyOpenFilter());
		filterTreeModel = new CrucibleFilterTreeModel(project, filterListModel, reviewListModel);
		this.reviewListModel = reviewListModel;
		CrucibleReviewListModel sortingListModel = new SortingByKeyCrucibleReviewListModel(this.reviewListModel);
		searchingReviewListModel = new SearchingCrucibleReviewListModel(sortingListModel);
		currentReviewListModel = searchingReviewListModel;
		init(Constants.DIALOG_MARGIN / 2);
		this.reviewListModel.addListener(new LocalCrucibleReviewListModelListener());
        addFilterTreeListeners();
        filterTree.addSelectionListener(new LocalCrucibleFilterListModelListener());
        ToolTipManager.sharedInstance().registerComponent(filterTree);
	}


    public void notifyCrucibleFilterListModelListeners(final CustomFilter customFilter) {
        for (CustomFilterChangeListener listener : customFilterChangeListeners) {
            listener.customFilterChanged(customFilter);
        }
    }
	@Override
	public void init(int margin) {
		super.init(margin);

		addReviewTreeListeners();
		setupReviewTree();

		initToolBar();

		customFilterChangeListeners.add(new CustomFilterChangeListener() {
			public void customFilterChanged(CustomFilter customFilter) {
				refresh(UpdateReason.FILTER_CHANGED);
			}
		});

		filterTreeModel.nodeChanged((DefaultMutableTreeNode) filterTreeModel.getRoot());

		if (crucibleProjectConfiguration != null
				&& crucibleProjectConfiguration.getCrucibleFilters().getManualFilter() != null
				&& crucibleProjectConfiguration.getCrucibleFilters().getManualFilter().isEnabled()) {
		}
		addSearchBoxListener();

        CrucibleFilterSelectionListener listener = new CrucibleFilterSelectionListenerAdapter() {

                public void selectedCustomFilter(CustomFilter customFilter) {
                    refresh(UpdateReason.REFRESH);
                }
            };

         filterTree.addSelectionListener(listener);

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

	public void openReview(final ReviewAdapter review, boolean retrieveDetails) {
		CommentHighlighter.removeCommentsInEditors(project);
		reviewListModel.openReview(review, UpdateReason.OPEN_IN_IDE);
		IdeaHelper.getReviewDetailsToolWindow(getProject()).showReview(review, retrieveDetails);
	}

	public void closeReviewDetailsWindow(final AnActionEvent event) {
		reviewListModel.clearOpenInIde(UpdateReason.OPEN_IN_IDE);
		IdeaHelper.getReviewDetailsToolWindow(project).closeToolWindow(event);
	}


    private void addFilterTreeListeners() {
        filterTree.addSelectionListener(new LocalCrucibleFilterListModelListener());
        filterTree.addMouseListener(new PopupAwareMouseAdapter() {

            protected void onPopup(MouseEvent e) {

                if (!(e.getComponent() instanceof FilterTree)) {
                    return;
                }
                FilterTree tree = (FilterTree) e.getComponent();
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                tree.setSelectionPath(path);
                Object o = path.getLastPathComponent();
                if (!(o instanceof CrucibleCustomFilterTreeNode)) {
                    return;
                }

                CustomFilter manualFilter = ((CrucibleCustomFilterTreeNode) o).getFilter();
                if (manualFilter != null) {
                    ActionManager aManager = ActionManager.getInstance();
                    ActionGroup menu = (ActionGroup) aManager.getAction("ThePlugin.Crucible.ManualFilterPopupGroup");
                    if (menu == null) {
                        return;
                    }
                    aManager.createActionPopupMenu(MANUAL_FILTER_MENU_PLACE, menu).getComponent()
                            .show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }


    private void addReviewTreeListeners() {
		reviewTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				final ReviewAdapter review = reviewTree.getSelectedReview();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && review != null) {
					openReview(review, true);
				}
			}
		});

		reviewTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final ReviewAdapter review = reviewTree.getSelectedReview();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && review != null) {
					openReview(review, true);
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
			if (dataId.equals(Constants.SERVER)) {
				if (reviewTree.getSelectedReview() != null) {
					return reviewTree.getSelectedReview().getServerData();
				}
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
			reviewTree = new ReviewTree(new ReviewTreeModel(currentReviewListModel, projectCfgManager));

			new TreeSpeedSearch(reviewTree) {
				@Override
				protected boolean isMatchingElement(Object o, String s) {
					TreePath tp = (TreePath) o;
					Object node = tp.getLastPathComponent();
					if (node instanceof CrucibleReviewTreeNode) {
						ReviewTreeNode rtn = (ReviewTreeNode) node;
						ReviewAdapter review = rtn.getReview();
						return review.getPermId().getId().toLowerCase().contains(s.toLowerCase())
								|| review.getName().toLowerCase().contains(s.toLowerCase());
					} else {
						return super.isMatchingElement(o, s);
					}
				}
			};
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


    public UiTaskExecutor getUiTaskExecutor() {
        return uiTaskExecutor;
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

	/*protected void showManualFilterPanel(boolean visible) {
		getSplitLeftPane().setOrientation(true);

		if (visible) {
			getSplitLeftPane().setSecondComponent(detailsPanel);
			getSplitLeftPane().setProportion(MANUAL_FILTER_PROPORTION_VISIBLE);

		} else {
			getSplitLeftPane().setSecondComponent(null);
			getSplitLeftPane().setProportion(MANUAL_FILTER_PROPORTION_HIDDEN);
		}
	}*/

	public void refresh(final UpdateReason reason) {
		Task.Backgroundable refresh = new Task.Backgroundable(getProject(), "Refreshing Crucible Panel", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				reviewListModel.rebuildModel(reason);
			}
		};
		ProgressManager.getInstance().run(refresh);
	}

	public Collection<ServerData> getServers() {
		return projectCfgManager.getAllEnabledCrucibleServerss();
	}

	public List<ReviewAdapter> getLocalReviews(final String searchKey) {
		List<ReviewAdapter> reviews = new ArrayList<ReviewAdapter>();
		for (ReviewAdapter review : currentReviewListModel.getReviews()) {
			if (searchKey.toUpperCase().equals(review.getPermId().getId().toUpperCase())) {
				reviews.add(review);
			}
		}

		return reviews;
	}

	public CrucibleWorkspaceConfiguration getCrucibleConfiguration() {
		return crucibleProjectConfiguration;
	}

	/**
	 * Blocking method. Should be called in the background thread.
	 *
	 * @param recentlyOpenReviews list of recenlty open reviews
	 * @return list of review adapters
	 */
	public List<ReviewAdapter> getReviewAdapters(final List<ReviewRecentlyOpenBean> recentlyOpenReviews) {

		List<ReviewAdapter> reviews = new ArrayList<ReviewAdapter>();

		if (recentlyOpenReviews == null || recentlyOpenReviews.isEmpty()) {
			return reviews;
		}

		for (ReviewRecentlyOpenBean recentlyOpenReview : recentlyOpenReviews) {
			// search local list for recently open reviews
			ReviewAdapter ra = getReviewFromLocalModel(recentlyOpenReview.getReviewId(), recentlyOpenReview.getServerId());

			if (ra != null) {
				reviews.add(ra);
			} else {
				// search review on the servers
				ReviewAdapter rra = getReviewFromServer(recentlyOpenReview.getReviewId(), recentlyOpenReview.getServerId());

				if (rra != null) {
					reviews.add(rra);
				}
			}
		}

		return reviews;
	}

	/**
	 * Blocking method. Should be called in the background thread.
	 *
	 * @param reviewKey review key
	 * @param serverId  server id
	 * @return review if found or null otherwise
	 */
	private ReviewAdapter getReviewFromServer(final String reviewKey, final ServerId serverId) {

		ServerData server = projectCfgManager.getEnabledCrucibleServerr(serverId);
		if (server != null) {
			try {
				Review r = CrucibleServerFacadeImpl.getInstance().getReview(server, new PermId(reviewKey));
				return new ReviewAdapter(r, server);
			} catch (RemoteApiException e) {
				PluginUtil.getLogger().warn("Exception thrown when retrieving review", e);
				setStatusErrorMessage("Cannot get review from the server: " + e.getMessage(), e);
			} catch (ServerPasswordNotProvidedException e) {
				PluginUtil.getLogger().warn("Exception thrown when retrieving review", e);
				setStatusErrorMessage("Cannot get review from the server: " + e.getMessage(), e);
			}
		}

		return null;
	}

	/**
	 * Blocking method. Should be called in the background thread.
	 *
	 * @param reviewKey review key
	 * @param serverId  server id
	 * @return review if found or null otherwise
	 */
	// todo remove that method if review contains details (ValueNotYetInitialized problem)
	private ReviewAdapter getReviewWithDetailsFromServer(final String reviewKey, final ServerId serverId) {

		ServerData server = projectCfgManager.getEnabledCrucibleServerr(serverId);
		if (server != null) {
			try {
				Review r = CrucibleServerFacadeImpl.getInstance().getReview(server, new PermId(reviewKey));
				ReviewAdapter ra = new ReviewAdapter(r, server);
				CrucibleServerFacadeImpl.getInstance().getDetailsForReview(ra);
				return ra;
			} catch (RemoteApiException e) {
				PluginUtil.getLogger().warn("Exception thrown when retrieving review", e);
				setStatusErrorMessage("Cannot get review from the server: " + e.getMessage(), e);
			} catch (ServerPasswordNotProvidedException e) {
				PluginUtil.getLogger().warn("Exception thrown when retrieving review", e);
				setStatusErrorMessage("Cannot get review from the server: " + e.getMessage(), e);
			}
		}

		return null;
	}

	private ReviewAdapter getReviewFromLocalModel(final String reviewKey, final ServerId serverId) {
		if (currentReviewListModel.getReviews() != null && !currentReviewListModel.getReviews().isEmpty()) {
			for (ReviewAdapter localReview : currentReviewListModel.getReviews()) {
				if (localReview.getPermId().getId().equals(reviewKey)
						&& localReview.getServerData().getServerId().equals(serverId)) {
					return localReview;
				}
			}
		}
		return null;
	}

	/**
	 * Should be called from the background thread. It downloads review from the server if not found in the local model.
	 *
	 * @param reviewKey
	 * @param serverUrl
	 * @return review
	 */
	public ReviewAdapter openReviewWithDetails(final String reviewKey, final String serverUrl) {
		ServerData server = CfgUtil.findServer(serverUrl, projectCfgManager.getAllCrucibleServerss());

		if (server != null) {
			// todo uncomment that if local review contains details
			// (will be implemented together with removed ValueNotYetInitialized)
//			ReviewAdapter ra = getReviewFromLocalModel(reviewKey, server.getServerId());
//			if (ra == null) {
//				ra = getReviewFromServer(reviewKey, server.getServerId());
//			}

			ReviewAdapter ra = getReviewWithDetailsFromServer(reviewKey, server.getServerId());

			if (ra != null) {
				final ReviewAdapter reviewAdapter = ra;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						openReview(reviewAdapter, false);
					}
				});

				return ra;
			} else {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						Messages.showInfoMessage(project, "Server " + serverUrl + " not found in configuration",
								PluginUtil.PRODUCT_NAME);
					}
				});
			}
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					Messages.showInfoMessage(project, "Server " + serverUrl + " not found in configuration",
							PluginUtil.PRODUCT_NAME);
				}
			});
		}
		return null;
	}

    private class LocalCrucibleFilterListModelListener extends CrucibleFilterSelectionListenerAdapter {
		public void filterSelectionChanged() {
			// restart checker
			refresh(UpdateReason.FILTER_CHANGED);
		}

	}

	private class LocalCrucibleReviewListModelListener extends CrucibleReviewListModelListenerAdapter {
		private Exception exception;

		@Override
		public void reviewListUpdateStarted(UpdateContext updateContext) {
			exception = null;
			if (updateContext.getUpdateReason() != UpdateReason.TIMER_FIRED) {
				setPanelEnabled(false);
				setStatusInfoMessage("Loading reviews...");
			}
		}

		@Override
		public void reviewListUpdateFinished(UpdateContext updateContext) {
			setPanelEnabled(true);
			if (exception != null) {
				setStatusErrorMessage(exception.getMessage(), exception);
			} else {
				setStatusInfoMessage("Loaded " + reviewListModel.getReviews().size() + " reviews");
			}
		}

		@Override
		public void reviewListUpdateError(final UpdateContext updateContext, final Exception e) {
			this.exception = e;
		}
	}
}
