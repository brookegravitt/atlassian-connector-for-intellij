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
import com.atlassian.theplugin.idea.crucible.tree.ReviewTreeModel;
import com.atlassian.theplugin.idea.jira.StatusBarPane;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewsToolWindowPanel extends JPanel implements DataProvider {

	public static final String PLACE_PREFIX = ReviewsToolWindowPanel.class.getSimpleName();

	private final CrucibleProjectConfiguration crucibleProjectCfg;
	private final CrucibleReviewListModel reviewListModel;

	private final Project project;
	private final CfgManager cfgManager;

	private static final float PANEL_SPLIT_RATIO = 0.3f;
	private final Splitter splitPane = new Splitter(true, PANEL_SPLIT_RATIO);


	// left panel
	private Splitter splitFilterPane;
	private JPanel serversPanel;
	private JTree serversTree;
	private JPanel manualFilterDetailsPanel;
	// right panel
	private JPanel reviewsPanel;
	private JScrollPane reviewTreeScrollPane;
	private JTree reviewTree;
	// bottom panel
	private StatusBarPane statusBarPane;

	public ReviewsToolWindowPanel(@NotNull final Project project,
			@NotNull final CrucibleProjectConfiguration crucibleProjectConfiguration,
			@NotNull final CfgManager cfgManager,
			@NotNull final CrucibleReviewListModel reviewListModel) {

		this.project = project;
		this.cfgManager = cfgManager;
		this.crucibleProjectCfg = crucibleProjectConfiguration;
		this.reviewListModel = reviewListModel;

		initialize();

	}

	private void initialize() {

		setLayout(new BorderLayout());

		splitPane.setFirstComponent(createLeftContent());
		splitPane.setSecondComponent(createRightContent());

		splitPane.setShowDividerControls(false);
		splitPane.setHonorComponentsMinimumSize(true);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension dimension = e.getComponent().getSize();
				final boolean doVertical = dimension.getWidth() < dimension.getHeight();
				if (doVertical != splitPane.getOrientation()) {
					splitPane.setOrientation(doVertical);
				}

			}
		});

		add(splitPane, BorderLayout.CENTER);

		add(createStatusBar(), BorderLayout.SOUTH);
	}

	protected JComponent createStatusBar() {
		statusBarPane = new StatusBarPane("Reviews panel");
		return statusBarPane;
	}

	protected JComponent createLeftContent() {
		splitFilterPane = new Splitter(false, 1.0f);
		splitFilterPane.setOrientation(true);

		serversPanel = new JPanel(new BorderLayout());

		serversTree = new JTree();
		JScrollPane filterListScrollPane = new JScrollPane(serversTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		filterListScrollPane.setWheelScrollingEnabled(true);

		manualFilterDetailsPanel = new JPanel();

		serversPanel.add(filterListScrollPane, BorderLayout.CENTER);
		serversPanel.add(createServersToolbar(), BorderLayout.NORTH);

		splitFilterPane.setFirstComponent(serversPanel);

		return splitFilterPane;
	}

	private JComponent createServersToolbar() {
		// todo create toolbar
		return new JLabel();
	}

	protected JComponent createRightContent() {
		reviewsPanel = new JPanel(new BorderLayout());

		reviewTreeScrollPane = new JScrollPane(createReviewsTree(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		reviewTreeScrollPane.setWheelScrollingEnabled(true);

		reviewsPanel.add(reviewTreeScrollPane, BorderLayout.CENTER);
		reviewsPanel.add(createReviewsToolbar(), BorderLayout.NORTH);
		return reviewsPanel;
	}

	private JTree createReviewsTree() {
		reviewTree = new JTree(new ReviewTreeModel(reviewListModel));
//		reviewTree = new JTree();
		addReviewTreeListeners();

		return reviewTree;
	}

	private JComponent createReviewsToolbar() {
		// todo create toolbar
		return new JLabel();
	}

	private ReviewAdapter getSelectedReview() {
		// todo: this is temporary, we will fix it later, when the tree is actually functional
		Collection<ReviewAdapter> reviews = reviewListModel.getReviews();
		if (reviews != null && reviews.size() > 0) {
			return reviews.iterator().next();
		}
		return null;
	}

	public void openReview(final ReviewAdapter review) {
		CrucibleReviewWindow.getInstance(project).showCrucibleReviewWindow(review);
	}

	private void addReviewTreeListeners() {
		reviewTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				final ReviewAdapter review = getSelectedReview();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && review != null) {
					openReview(review);
				}
			}
		});

		reviewTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final ReviewAdapter review = getSelectedReview();
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
					final ReviewAdapter review = getSelectedReview();
					if (review != null) {
						launchContextMenu(e);
					}
				}
			}
		});
	}

	private String getPlaceName() {
		return PLACE_PREFIX + this.project.getName();
	}

	private void launchContextMenu(MouseEvent e) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();

		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.Reviews.ReviewPopupMenu");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(getPlaceName(), actionGroup);

		final JPopupMenu jPopupMenu = popup.getComponent();
		jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	@Nullable
	public Object getData(@NonNls String dataId) {
		if (dataId.equals(Constants.REVIEW)) {
			return getSelectedReview();
		}
		return null;

	}
}
