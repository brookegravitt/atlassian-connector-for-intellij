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
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTreeModel;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
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
	private JTree reviewTree;

	public ReviewsToolWindowPanel(@NotNull final Project project,
			@NotNull final CrucibleProjectConfiguration crucibleProjectConfiguration,
			@NotNull final CfgManager cfgManager,
			@NotNull final CrucibleReviewListModel reviewListModel) {
		super(project, cfgManager, "ThePlugin.Reviews.StatusesToolbar", "ThePlugin.Reviews.ReviewsToolbar");

		this.reviewListModel = reviewListModel;
		init();
		addReviewTreeListeners();
		setupReviewTree();
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
			reviewTree = new JTree(new ReviewTreeModel(reviewListModel));
		}
		return reviewTree;
	}

	public JTree createLeftTree() {
		return new JTree();
	}

	public void onEditButtonClickAction() {		
	}

	public String getActionPlaceName() {
		return PLACE_PREFIX + this.getProject().getName();
	}

	private void setupReviewTree() {
		TreeUISetup uiSetup = new TreeUISetup(TREE_RENDERER);
		uiSetup.initializeUI(reviewTree, getRightScrollPane());
		final JTree finalReviewTree = getRightTree();

		reviewTree.setShowsRootHandles(true);
		reviewTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		reviewTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				final TreePath selectionPath = finalReviewTree.getSelectionModel().getSelectionPath();
				if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
					((AbstractTreeNode) selectionPath.getLastPathComponent()).onSelect();
				}
			}
		});
	}
}
