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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.FocusOnFileEvent;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
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


			reviewFilesTree.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent event) {
					//To change body of implemented methods use File | Settings | File Templates.
				}

				public void mousePressed(MouseEvent e) {
					int selRow = reviewFilesTree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = reviewFilesTree.getPathForLocation(e.getX(), e.getY());
					if (selRow != -1) {
						if (e.getClickCount() == 2) {
							nodeClicked(selRow, selPath);
						}
					}
				}

				public void mouseReleased(MouseEvent event) {
					//To change body of implemented methods use File | Settings | File Templates.
				}

				public void mouseEntered(MouseEvent event) {
					//To change body of implemented methods use File | Settings | File Templates.
				}

				public void mouseExited(MouseEvent event) {
					//To change body of implemented methods use File | Settings | File Templates.
				}
			});
			
			reviewFilesTree.setCellRenderer(new CrucibleTreeRenderer());
		}

		return reviewFilesTree;
	}

	private void nodeClicked(int selRow, TreePath path) {
		if (path != null) {
			selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (selectedNode instanceof GeneralCommentNode) {
				// GeneralComment server = ((GeneralCommentNode) selectedNode).getGeneralComment();
			} else if (selectedNode instanceof ReviewItemDataNode) {
				IdeaHelper.getCurrentReviewActionEventBroker().trigger(
						new FocusOnFileEvent(
								ReviewItemTreePanel.this,
								((CrucibleTreeRootNode) model.getRoot()).getReviewDataInfoAdapter(),
								((ReviewItemDataNode) selectedNode).getReviewItem()));
			}
		}
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

	public void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, VersionedComment versionedComment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
