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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleReviewTreeModel;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleTreeRenderer;
import com.atlassian.theplugin.idea.crucible.tree.CrucibleTreeRootNode;
import com.atlassian.theplugin.idea.crucible.tree.GeneralCommentNode;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemDataNode;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.util.Collection;

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
		add(new JLabel("File/Comment tree"), BorderLayout.NORTH);
		add(new JScrollPane(getReviewItemTree()), BorderLayout.CENTER);

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

			reviewFilesTree.setRootVisible(true);
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
							IdeaHelper.getCurrentReviewActionEventBroker().focusOnFile(
									ReviewItemTreePanel.this,
									((CrucibleTreeRootNode)model.getRoot()).getReviewDataInfoAdapter(),
									((ReviewItemDataNode)selectedNode).getReviewItem());
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

	//show list of files
	private void updateFilesTree(Server crucibleServer, ReviewItem fileItem, PermId reviewPermId) {
//		currentFileItem = fileItem;
//
//
//		ReviewItemDataNode reviewItemDataNode = model.getOrInsertReviewItemDataNode(fileItem, true);
//		TreePath itemNodePath = new TreePath(reviewItemDataNode.getPath());
//		boolean doExpand = reviewFilesTree.isExpanded(itemNodePath);

//		reviewItemDataNode.removeAllChildren();

//		Collection<VersionedComment> fileVersionedComments = null;
//		try {
//			fileVersionedComments = crucibleServerFacade.getVersionedComments(crucibleServer, reviewPermId, fileItem.getPermId());
//			for (VersionedComment comment : fileVersionedComments) {
//				model.insertNodeInto(new VersionedCommentNode(comment), reviewItemDataNode, reviewItemDataNode.getChildCount());
//			}
//		} catch (RemoteApiException e) {
//			PluginUtil.getLogger().error("Cannot retrieve comments : " + e.getMessage());
//		} catch (ServerPasswordNotProvidedException e) {
//			PluginUtil.getLogger().error("Password not provided: " + e.getMessage());
//		}
//
//		model.nodeStructureChanged(reviewItemDataNode);
//
//		if (doExpand) {
//			reviewFilesTree.expandPath(itemNodePath);
//		}
	}


	private void updateTreeConfiguration(ReviewDataInfoAdapter reviewAdapter) {
		Collection<ReviewItem> reviewFiles = null;
		Collection<GeneralComment> generalComments = null;
		firstItemNode = null;
		newSelectedNode = selectedNode;

		model.setRoot(new CrucibleTreeRootNode(reviewAdapter));
		try {
			reviewFiles = crucibleServerFacade.getReviewItems(reviewAdapter.getServer(), reviewAdapter.getPermaId());
			for (ReviewItem reviewFile : reviewFiles) {
				model.getOrInsertReviewItemDataNode(reviewFile, true);
//				updateFilesTree(reviewAdapter.getServer(), reviewFile, reviewAdapter.getPermaId());
			}

//			generalComments = crucibleServerFacade.getGeneralComments(reviewAdapter.getServer(), reviewAdapter.getPermaId());
//			for (GeneralComment generalComment: generalComments) {
//				model.getGeneralCommentNode(generalComment, true);
//			}
		} catch (RemoteApiException e) {
			PluginUtil.getLogger().error("Remote api error" + e.getMessage());
		} catch (ServerPasswordNotProvidedException e) {
			PluginUtil.getLogger().error("Password not provided: " + e.getMessage());
		}


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


	public void focusOnReview(ReviewDataInfoAdapter reviewItem) {
		 updateTreeConfiguration(reviewItem);
	}

	public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
