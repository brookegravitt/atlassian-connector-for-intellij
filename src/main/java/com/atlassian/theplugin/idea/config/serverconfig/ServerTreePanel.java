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

package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.ProductServerConfiguration;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.idea.config.serverconfig.model.*;
import com.atlassian.theplugin.idea.config.serverconfig.util.ServerNameUtil;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Collection;

public final class ServerTreePanel extends JPanel implements TreeSelectionListener {

	private JTree serverTree = null;
	private transient PluginConfiguration pluginConfiguration = null;
	private ServerTreeModel model;
	private DefaultMutableTreeNode selectedNode = null;
	private DefaultMutableTreeNode newSelectedNode = null;
	private DefaultMutableTreeNode firstServerNode = null;
	private boolean forceExpand = true;

	private static final int WIDTH = 150;
	private static final int HEIGHT = 250;
	private static final int VISIBLE_ROW_COUNT = 7;
	private static ServerTreePanel instance;

	/**
	 * serverConfigPanel needs to be initialized outside of the constructor to avoid cyclic dependency.
	 * @param serverConfigPanel panel to invoke storeServer() and showEmptyPanel() on.
	 */
	public void setServerConfigPanel(ServerConfigPanel serverConfigPanel) {
		this.serverConfigPanel = serverConfigPanel;
	}

	private ServerConfigPanel serverConfigPanel;

	private ServerTreePanel() {
		initLayout();
	}

	public static ServerTreePanel getInstance() {
		if (instance == null) {
			instance = new ServerTreePanel();
		}
		return instance;
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		add(new JScrollPane(getServerTree()), BorderLayout.CENTER);
	}

	private void expandAllPaths() {
		for (int i = 0; i < serverTree.getRowCount(); ++i) {
                 serverTree.expandRow(i);
        }
    }

	private JTree getServerTree() {
		if (serverTree == null) {
			serverTree = new JTree();
			
			serverTree.setName("Server tree");

			RootNode root = new RootNode();
			model = new ServerTreeModel(root);
			serverTree.setModel(model);

			serverTree.setRootVisible(false);
			serverTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			serverTree.setVisibleRowCount(VISIBLE_ROW_COUNT);
			serverTree.setShowsRootHandles(true);

			serverTree.addTreeSelectionListener(this);
			

			serverTree.setCellRenderer(new ServerTreeRenderer());
		}
		return serverTree;
	}

	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getServerTree().setEnabled(b);
	}

	public String addServer(ServerType serverType) {
		Server newServer = new ServerBean();

		Collection<Server> servers = pluginConfiguration.getProductServers(serverType).transientGetServers();
		newServer.setName(ServerNameUtil.suggestNewName(servers));
		pluginConfiguration.getProductServers(serverType).storeServer(newServer);

		ServerNode child = ServerNodeFactory.getServerNode(serverType, newServer);
		ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType, true);
		model.insertNodeInto(child, serverTypeNode, serverTypeNode.getChildCount());

		TreePath path = new TreePath(child.getPath());
		serverTree.scrollPathToVisible(path);
		serverTree.setSelectionPath(path);
		serverTree.expandPath(path);

		return newServer.getName();
	}


	public void copyServer() {
/*
		ServerBean newServer = new ServerBean();
		newServer.setName(suggestCopyName(ConfigurationFactory.getConfiguration()
				.getBambooConfiguration().transientGetServers()));
		ConfigurationFactory.getConfiguration().getBambooConfiguration().addServer(newServer);
		serverTree.updateUI();
		return newServer.getName();
*/
	}

	public void removeServer() {
		if (selectedNode != null) {
			if (selectedNode instanceof ServerNode) {
				final ServerNode selectedServerNode = (ServerNode) this.selectedNode;
				int response = Messages.showYesNoDialog(
						"Are you sure you want to delete the selected server?",
						"Confirm server delete",
						Messages.getQuestionIcon()						
						);

				if (response != 0) {
					return;
				}
				final ProductServerConfiguration productServers =
						pluginConfiguration.getProductServers(selectedServerNode.getServerType());

				productServers.removeServer(selectedServerNode.getServer());

				TreeNode parent = selectedServerNode.getParent();
				selectedServerNode.removeFromParent();
				model.nodeStructureChanged(parent);
			}
		}
	}

	public void setData(PluginConfiguration aPluginConfiguration) {
		// jgorycki: I assume this method will only be called at the beginning of the dialog's lifecycle.
		// I want to expand all paths in the tree and not select any nodes - hence showing an empty panel
		this.pluginConfiguration = aPluginConfiguration;
		updateTreeConfiguration();
		if (forceExpand) {
			// do this only during first operation
			expandAllPaths();
			forceExpand = false;
		}
	}

	private void updateServerTree(ServerType serverType) {
		Collection<Server> servers = pluginConfiguration.getProductServers(serverType).transientGetServers();

		if (servers.isEmpty()) {
			// remove from list if it was there for any reason
			ServerTypeNode toRemove = model.getServerTypeNode(serverType, false);
			if (toRemove != null) {
				TreeNode root = toRemove.getParent();
				toRemove.removeFromParent();
				model.nodeStructureChanged(root);
			}
		} else {

			// !servers.isEmpty() because:
			// if server list is empty, don't create server type node,
			// otherwise create node - it would be required
			ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType, !servers.isEmpty());
			TreePath serverNodePath = new TreePath(serverTypeNode.getPath());
			boolean doExpand = serverTree.isExpanded(serverNodePath);

			serverTypeNode.removeAllChildren();

			for (Server server : servers) {
				ServerNode child = ServerNodeFactory.getServerNode(serverType, server);

				model.insertNodeInto(child, serverTypeNode, serverTypeNode.getChildCount());

				if (firstServerNode == null) {
					firstServerNode = child;
				}

				if (selectedNode != null && selectedNode instanceof ServerNode) {
					if (child.getServer().equals(((ServerNode) selectedNode).getServer())) {
						newSelectedNode = child;
					}
				}
			}
			model.nodeStructureChanged(serverTypeNode);

			if (doExpand) {
				serverTree.expandPath(serverNodePath);
			}
		}
	}

	private void updateTreeConfiguration() {
		firstServerNode = null;
		newSelectedNode = selectedNode;

		for (ServerType serverType : ServerType.values()) {
			updateServerTree(serverType);
		}

		if (newSelectedNode != null) {
			selectedNode = newSelectedNode;
			TreePath path = new TreePath(selectedNode.getPath());
			serverTree.scrollPathToVisible(path);
			serverTree.setSelectionPath(path);
		} else {
			selectedNode = null;
			serverConfigPanel.showEmptyPanel();
			if (firstServerNode != null) {
				TreePath path = new TreePath(firstServerNode.getPath());
				serverTree.scrollPathToVisible(path);
				serverTree.setSelectionPath(path);
				serverTree.expandPath(path);
			}
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();

		if (path != null) {
			TreePath oldPath = e.getOldLeadSelectionPath();
			if (oldPath != null) {
				DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) oldPath.getLastPathComponent();
				if (oldNode != null && oldNode instanceof ServerNode) {
					serverConfigPanel.storeServer((ServerNode) oldNode);
				}
			}
			selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (selectedNode instanceof ServerNode) {
				Server server = ((ServerNode) selectedNode).getServer();
				if (pluginConfiguration.isServerPresent(server)) {
					serverConfigPanel.editServer(
							((ServerNode) selectedNode).getServerType(), server);
				} else {
					// PL-235 show blank panel if server from tree node does not exist in configuration
					// it happens if you add server, click cancel and open config window again
					serverConfigPanel.showEmptyPanel();
				}
			} else if (selectedNode instanceof ServerTypeNode) {
				serverConfigPanel.showEmptyPanel();
			}
		} else {
			serverConfigPanel.showEmptyPanel();
		}
	}
}
