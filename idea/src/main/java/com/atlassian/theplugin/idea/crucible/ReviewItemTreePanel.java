package com.atlassian.theplugin.idea.crucible;
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
public class ReviewItemTreePanel  extends JPanel implements TreeSelectionListener, TableItemSelectedListener {

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
		crucibleServerFacade =  CrucibleServerFacadeImpl.getInstance();
		crucibleTableToolWindowPanel =
				CrucibleTableToolWindowPanel.getInstance(projectConfigurationBean);

		crucibleTableToolWindowPanel.addItemSelectedListener(this);
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

			reviewFilesTree.addTreeSelectionListener(this);

			reviewFilesTree.setCellRenderer(new CrucibleTreeRenderer());
		}

		return reviewFilesTree;
	}

	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getReviewItemTree().setEnabled(b);
	}

	//show list of files
	private void updateReviewItemTree(Server crucibleServer, ReviewItem fileItem, PermId reviewPermId) {
		currentFileItem = fileItem;


			ReviewItemDataNode reviewItemDataNode = model.getReviewItemDataNode(fileItem, true);
			TreePath itemNodePath = new TreePath(reviewItemDataNode.getPath());
			boolean doExpand = reviewFilesTree.isExpanded(itemNodePath);

			reviewItemDataNode.removeAllChildren();

			Collection<GeneralComment> reviewComments = null;
			try {
				reviewComments = crucibleServerFacade.getComments(crucibleServer, reviewPermId);
			} catch (RemoteApiException e) {
				PluginUtil.getLogger().error("Cannot retrieve comments : " + e.getMessage());
			} catch (ServerPasswordNotProvidedException e) {
				PluginUtil.getLogger().error("Password not provided: " + e.getMessage());
			}

			for (GeneralComment comment: reviewComments) {
				model.insertNodeInto(new CommentNode(comment), reviewItemDataNode, reviewItemDataNode.getChildCount());
			}

//				if (firstCommentNode == null) {
//					firstCommentNode = child;
//				}
//
//				if (selectedNode != null && selectedNode instanceof CommentNode) {
//					if (child.getServer().equals(((ServerNode) selectedNode).getServer())) {
//						newSelectedNode = child;
//					}
//				}

			model.nodeStructureChanged(reviewItemDataNode);

			if (doExpand) {
				reviewFilesTree.expandPath(itemNodePath);
			}
	}


	private void updateTreeConfiguration(ReviewDataInfoAdapter reviewAdapter) {
		Collection<ReviewItem> reviewFiles = null;
		firstItemNode = null;
		newSelectedNode = selectedNode;

		model.setRoot(new CrucibleTreeRootNode(reviewAdapter));
		try {
			reviewFiles = crucibleServerFacade.getReviewItems(reviewAdapter.getServer(), reviewAdapter.getPermaId());

		} catch (RemoteApiException e) {
			PluginUtil.getLogger().error("Remote api error" + e.getMessage());
		} catch (ServerPasswordNotProvidedException e) {
			PluginUtil.getLogger().error("Password not provided: " + e.getMessage());
		}


		for (ReviewItem reviewFile: reviewFiles) {

		//ReviewItemDataNode fileNode = model.getReviewItemDataNode(reviewFile, true);
		//model.insertNodeInto(child, serverTypeNode, serverTypeNode.getChildCount());

//		TreePath path = new TreePath(child.getPath());
//		reviewFilesTree.scrollPathToVisible(path);
//		reviewFilesTree.setSelectionPath(path);
//		reviewFilesTree.expandPath(path);

			updateReviewItemTree(reviewAdapter.getServer(), reviewFile, reviewAdapter.getPermaId());
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

	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();

		if (path != null) {
			TreePath oldPath = e.getOldLeadSelectionPath();
			if (oldPath != null) {
				DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) oldPath.getLastPathComponent();
				if (oldNode != null && oldNode instanceof CommentNode) {
					//@todo ???serverConfigPanel.storeServer((CommentNode) oldNode);
				}
			}
			selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (selectedNode instanceof CommentNode) {
				GeneralComment server = ((CommentNode) selectedNode).getGeneralComment();
//				if (pluginConfiguration.isServerPresent(server)) {
//					//@todo show on the right details of .... serverConfigPanel.editServer(
////							((ServerNode) selectedNode).getServerType(), server);
//				} else {
//					// PL-235 show blank panel if server from tree node does not exist in configuration
//					// it happens if you add server, click cancel and open config window again
//					//@todo check serverConfigPanel.showEmptyPanel();
//				}
			} else if (selectedNode instanceof ReviewItemDataNode) {
				//@todo serverConfigPanel.showEmptyPanel();
			}
		} else {
			//@todo serverConfigPanel.showEmptyPanel();
//		}
	}

}
	//@todo: Change to thread
	public void itemSelected(Object item, int noClicks) {
		if (item != null && item instanceof ReviewDataInfoAdapter) {
			ReviewDataInfoAdapter review = (ReviewDataInfoAdapter) item;


			updateTreeConfiguration(review);
		} else {
			PluginUtil.getLogger().error("WTFFFFFFFFFFFFFFF");
		}
	}
	}
