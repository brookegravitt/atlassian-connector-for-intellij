package com.atlassian.theplugin.idea.crucible.tree;
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

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleReviewTreeModel;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleTreeRenderer;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleTreeRootNode;
import com.atlassian.theplugin.idea.crucible.tree.GeneralCommentNode;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemDataNode;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.events.FocusOnFileEvent;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.intellij.util.ui.UIUtil;
import com.intellij.ui.TreeList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 11, 2008
 * Time: 10:56:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewItemTreePanel extends JPanel
		implements CrucibleReviewActionListener {

	private JTree reviewFilesTree = null;
	private JList list = null;
	private CrucibleReviewTreeModel model;
	private DefaultMutableTreeNode selectedNode = null;
	private DefaultMutableTreeNode newSelectedNode = null;
	private DefaultMutableTreeNode firstItemNode = null;
	private boolean forceExpand = true;

	private static final int WIDTH = 150;
	private static final int HEIGHT = 250;
	private static final int VISIBLE_ROW_COUNT = 7;
	private static ReviewItemTreePanel instance;
	private static CrucibleServerFacade crucibleServerFacade;

	private static ReviewItem currentFileItem;
	private static CrucibleTableToolWindowPanel crucibleTableToolWindowPanel;
	public static final Logger LOGGER = PluginUtil.getLogger();

	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private JLabel statusLabel;

	private ReviewItemTreePanel(ProjectConfigurationBean projectConfigurationBean) {
		initLayout();
		IdeaHelper.getCurrentReviewActionEventBroker().registerListener(this);
		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		crucibleTableToolWindowPanel =
				CrucibleTableToolWindowPanel.getInstance(projectConfigurationBean);
	}


	public static ReviewItemTreePanel getInstance(ProjectConfigurationBean projectConfigurationBean) {
		if (instance == null) {
			instance = new ReviewItemTreePanel(projectConfigurationBean);
		}
		return instance;
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		add(new JLabel("File list"), BorderLayout.NORTH);
		add(new JScrollPane(getReviewItemTree()), BorderLayout.CENTER);
		statusLabel = new JLabel();
		statusLabel.setBackground(UIUtil.getTreeTextBackground());
		add(statusLabel, BorderLayout.SOUTH);
	}

	private void expandAllPaths() {
		for (int i = 0; i < reviewFilesTree.getRowCount(); ++i) {
			reviewFilesTree.expandRow(i);
		}
	}

	private JTree getReviewItemTree() {
		if (reviewFilesTree == null) {
			reviewFilesTree = new JTree();

			reviewFilesTree.setName("Server tree");
			CrucibleTreeRootNode root = new CrucibleTreeRootNode();

			model = new CrucibleReviewTreeModel(root);
			reviewFilesTree.setModel(model);

			reviewFilesTree.setRootVisible(false);
			reviewFilesTree.expandRow(0);
			reviewFilesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			reviewFilesTree.setVisibleRowCount(VISIBLE_ROW_COUNT);
			reviewFilesTree.setShowsRootHandles(true);

			reviewFilesTree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent event) {
					TreePath path = event.getNewLeadSelectionPath();

					if (path != null) {
						TreePath oldPath = event.getOldLeadSelectionPath();
						if (oldPath != null) {
							DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) oldPath.getLastPathComponent();
							if (oldNode != null && oldNode instanceof GeneralCommentNode) {
								//@todo ???serverConfigPanel.storeServer((GeneralCommentNode) oldNode);
							}
						}
						selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
						if (selectedNode instanceof GeneralCommentNode) {
							GeneralComment server = ((GeneralCommentNode) selectedNode).getGeneralComment();
						} else if (selectedNode instanceof ReviewItemDataNode) {
							IdeaHelper.getCurrentReviewActionEventBroker().trigger(
									new FocusOnFileEvent(
											ReviewItemTreePanel.this,
											((CrucibleTreeRootNode) model.getRoot()).getReviewDataInfoAdapter(),
											((ReviewItemDataNode) selectedNode).getReviewItem()));
						}
					} else {
						//@todo serverConfigPanel.showEmptyPanel();
//		}
					}

				}
			});

			reviewFilesTree.setCellRenderer(new CrucibleTreeRenderer());
		}

		return reviewFilesTree;
	}

	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getReviewItemTree().setEnabled(b);
	}


	private void updateTreeConfiguration(ReviewDataInfoAdapter reviewAdapter, Collection<ReviewItem> reviewFiles) {
		firstItemNode = null;
		newSelectedNode = selectedNode;

		model.setRoot(new CrucibleTreeRootNode(reviewAdapter));
		for (ReviewItem reviewFile : reviewFiles) {
			model.getOrInsertReviewItemDataNode(reviewFile, true);
		}
		reviewFilesTree.setRootVisible(true);
		reviewFilesTree.expandRow(0);
		if (newSelectedNode != null) {
			selectedNode = newSelectedNode;
			TreePath path = new TreePath(selectedNode.getPath());
			reviewFilesTree.scrollPathToVisible(path);
			reviewFilesTree.setSelectionPath(path);
		} else {
			selectedNode = null;
			//@todo: show Empy panel -- serverConfigPanel.showEmptyPanel();
			if (firstItemNode != null) {
				TreePath path = new TreePath(firstItemNode.getPath());
				reviewFilesTree.scrollPathToVisible(path);
				reviewFilesTree.setSelectionPath(path);
				reviewFilesTree.expandPath(path);
			}
		}
	}

	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}


	public void focusOnReview(final ReviewDataInfoAdapter reviewItem) {
		progressAnimation.startProgressAnimation();
		try {
			final List<ReviewItem> reviewFiles = crucibleServerFacade.getReviewItems(reviewItem.getServer(), reviewItem.getPermaId());
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					StringBuffer buffer = new StringBuffer();
					buffer.append("<html>");
					buffer.append("<body>");
					buffer.append(reviewItem.getCreator());
					buffer.append(" ");
					buffer.append("<font size=-1 color=");
					buffer.append(Constants.CRUCIBLE_AUTH_COLOR);
					buffer.append(">AUTH</font>");
					buffer.append(" ");
					if (!reviewItem.getCreator().equals(reviewItem.getModerator())) {
						buffer.append(reviewItem.getModerator());
					}
					buffer.append(" ");
					buffer.append("<font size=-1 color=");
					buffer.append(Constants.CRUCIBLE_MOD_COLOR);
					buffer.append(">MOD</font>");
					int i = 0;
					List<Reviewer> reviewers = reviewItem.getReviewers();
					if (reviewers != null) {
						buffer.append("<br>");
						for (Reviewer reviewer : reviewers) {
							if (i > 0) {
								buffer.append(", ");
							}
							buffer.append(reviewer.getDisplayName());
							i++;
						}
					}
					buffer.append("</body>");
					buffer.append("</html>");
					statusLabel.setText(buffer.toString());
					updateTreeConfiguration(reviewItem, reviewFiles);
				}
			});
		} catch (RemoteApiException e) {
			LOGGER.warn("Error retrieving the list of files attached to a review", e);
		} catch (ServerPasswordNotProvidedException e) {
			LOGGER.warn("Error retrieving the list of files attached to a review", e);
		} finally {
			progressAnimation.stopProgressAnimation();
		}
	}

	public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
