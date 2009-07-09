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
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.serverconfig.model.*;
import com.atlassian.theplugin.idea.config.serverconfig.util.ServerNameUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Collection;

public final class ServerTreePanel extends JPanel implements TreeSelectionListener, DataProvider {
	private JTree serverTree;
	private ServerTreeModel model;
	private DefaultMutableTreeNode selectedNode;
	private boolean forceExpand = true;

	public static final int WIDTH = 240;
	public static final int HEIGHT = 250;
	private static final int VISIBLE_ROW_COUNT = 7;
	private Collection<ServerCfg> servers;
	private Project project;

	/**
	 * serverConfigPanel needs to be initialized outside of the constructor to avoid cyclic dependency.
	 *
	 * @param serverConfigPanel panel to invoke storeServer() and showEmptyPanel() on.
	 */
	public void setServerConfigPanel(ServerConfigPanel serverConfigPanel) {
		this.serverConfigPanel = serverConfigPanel;
	}

	private ServerConfigPanel serverConfigPanel;

	public ServerTreePanel(Project project) {
		this.project = project;
		initLayout();
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

			model = new ServerTreeModel(new RootNode());
			serverTree.setModel(model);

			serverTree.setRootVisible(false);
			serverTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			serverTree.setVisibleRowCount(VISIBLE_ROW_COUNT);
			serverTree.setShowsRootHandles(true);

			serverTree.addTreeSelectionListener(this);

			final ServerTreeRenderer treeRenderer = new ServerTreeRenderer();
			serverTree.setCellRenderer(treeRenderer);
//			final ServerTreeMouseListener serverTreeMouseListener =
			new ServerTreeMouseListener(serverTree);

		}
		return serverTree;
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getServerTree().setEnabled(b);
	}

	public String addServer(ServerType serverType) {
		if (serverType == null) {
			return null;
		}

		String name = ServerNameUtil.suggestNewName(servers);
		ServerCfg newServer = createNewServer(serverType, name);

		if (newServer == null) {
			return null;
		}

		ServerNode child = addNewServerCfg(serverType, newServer);

		TreePath path = new TreePath(child.getPath());
		serverTree.scrollPathToVisible(path);
		serverTree.setSelectionPath(path);
		serverTree.expandPath(path);

		return newServer.getName();
	}

	public ServerNode addNewServerCfg(ServerType serverType, ServerCfg newServer) {
		servers.add(newServer);

		ServerNode child = ServerNodeFactory.getServerNode(newServer);
		ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType);
		model.insertNodeInto(child, serverTypeNode, serverTypeNode.getChildCount());
		return child;
	}

	private ServerCfg createNewServer(final ServerType serverType, final String name) {
		ServerIdImpl id = new ServerIdImpl();
		// CHECKSTYLE:OFF
		switch (serverType) {
			// CHECKSTYLE:ON
			case BAMBOO_SERVER:
				return new BambooServerCfg(false, name, id);
			case CRUCIBLE_SERVER:
				return new CrucibleServerCfg(false, name, id);
			case JIRA_SERVER:
				return new JiraServerCfg(false, name, id);
			case FISHEYE_SERVER:
				return new FishEyeServerCfg(false, name, id);
			case JIRA_STUDIO_SERVER:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						new JiraStudioConfigDialog(project, ServerTreePanel.this).show();
					}
				});
				return null;
		}
		throw new RuntimeException("Unhandled server type [" + serverType + "]");
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

				servers.remove(selectedServerNode.getServer());
				TreeNode parent = selectedServerNode.getParent();
				selectedServerNode.removeFromParent();
				model.nodeStructureChanged(parent);
			}
		}
	}

	public void setData(Collection<ServerCfg> newServers) {
		servers = newServers;
		// jgorycki: I assume this method will only be called at the beginning of the dialog's lifecycle.
		// I want to expand all paths in the tree and not select any nodes - hence showing an empty panel
		updateTreeConfiguration();
		if (forceExpand) {
			// do this only during first operation
			expandAllPaths();
//			forceExpand = false;
		}
	}

	private ServerNode updateServerTree() {
		ServerNode firstServerNode = null;

		model = new ServerTreeModel(new RootNode());
		serverTree.setModel(model);

		// !servers.isEmpty() because:
		// if server list is empty, don't create server type node,
		// otherwise create node - it would be required
//			ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType, !servers.isEmpty());
//			TreePath serverNodePath = new TreePath(serverTypeNode.getPath());
//			boolean doExpand = serverTree.isExpanded(serverNodePath);

		for (ServerCfg server : servers) {
			ServerNode child = ServerNodeFactory.getServerNode(server);
			ServerTypeNode serverTypeNode = model.getServerTypeNode(server.getServerType());

			model.insertNodeInto(child, serverTypeNode, serverTypeNode.getChildCount());

			if (firstServerNode == null) {
				firstServerNode = child;
			}

			if (selectedNode != null && selectedNode instanceof ServerNode) {
				ServerNode serverNode = (ServerNode) selectedNode;
				if (child.getServer().getServerId().equals(serverNode.getServer().getServerId())) {
					firstServerNode = child;
				}
			}
			model.nodeStructureChanged(serverTypeNode);
		}

//			if (doExpand) {
//				serverTree.expandPath(serverNodePath);
//			}
		return firstServerNode;
	}

	private void updateTreeConfiguration() {
		//DefaultMutableTreeNode tmpNode = selectedNode;
		selectedNode = updateServerTree();
		if (selectedNode != null) {
			TreePath path = new TreePath(selectedNode.getPath());
			serverTree.scrollPathToVisible(path);
			serverTree.setSelectionPath(path);
			serverTree.expandPath(path);
		} else {
			serverConfigPanel.showEmptyPanel();
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath oldPath = e.getOldLeadSelectionPath();
		if (oldPath != null) {
			DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) oldPath.getLastPathComponent();
			if (oldNode != null && oldNode instanceof ServerNode) {
				serverConfigPanel.saveData(((ServerNode) oldNode).getServerType());
			}
			model.nodeChanged(oldNode);

		}

		TreePath path = e.getNewLeadSelectionPath();

		if (path != null) {
			selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (selectedNode instanceof ServerNode) {
				ServerCfg server = ((ServerNode) selectedNode).getServer();
				serverConfigPanel.editServer(server);
//                else {
//					// PL-235 show blank panel if server from tree node does not exist in configuration
//					// it happens if you add server, click cancel and open config window again
//					serverConfigPanel.showEmptyPanel();
//				}
			} else if (selectedNode instanceof ServerTypeNode) {
				serverConfigPanel.showEmptyPanel();
			} else if (selectedNode instanceof ServerInfoNode) {
				serverConfigPanel.showEmptyPanel();
			}
		} else {
			serverConfigPanel.showEmptyPanel();
		}
	}


	public ServerCfg getSelectedServer() {
		if (selectedNode instanceof ServerNode) {
			return ((ServerNode) selectedNode).getServer();
		}
		return null;
	}

	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.SERVER_CFG_KEY.getName())) {
			final ServerCfg selectedServer = getSelectedServer();
			if (selectedServer == null) {
				return null;
			}
			return selectedServer;
		} else if (dataId.equals(Constants.SERVER_TYPE)) {
			if (selectedNode instanceof ServerTypeNode) {
				final ServerTypeNode serverTypeNode = (ServerTypeNode) selectedNode;
				return serverTypeNode.getServerType();
			}
		}
		return null;
	}

	public void setSelectedServer(final ServerData selectedServer) {
		if (selectedServer != null) {
			for (int i = 0; i < serverTree.getRowCount(); i++) {
				TreePath path = serverTree.getPathForRow(i);
				Object object = path.getLastPathComponent();
				if (object instanceof ServerNode) {
					ServerNode node = (ServerNode) object;
					if (node.getServer().getServerId().equals(selectedServer.getServerId())) {
						serverTree.expandPath(path);
						serverTree.makeVisible(path);
						serverTree.setSelectionPath(path);
						break;
					}
				}
			}
		}
	}

	public ServerType getSelectedServerType() {
		TreePath path = serverTree.getSelectionPath();
		if (path == null) {
			return null;
		}

		selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (selectedNode instanceof ServerTypeNode) {
			return ((ServerTypeNode) selectedNode).getServerType();
		}
		return null;
	}

	public class ServerTreeMouseListener extends MouseAdapter implements MouseMotionListener {
		private final JTree jtree;

		public ServerTreeMouseListener(@NotNull JTree jtree) {
			this.jtree = jtree;
			jtree.addMouseListener(this);
			jtree.addMouseMotionListener(this);
//			jtree.addComponentListener(this);

		}

		public void mousePressed(final MouseEvent e) {
			if (isHLinkHit(e)) {
				TreePath treepath = jtree.getPathForLocation(e.getX(), e.getY());
				if (treepath != null) {
					final Object o = treepath.getLastPathComponent();
					if (o instanceof ServerInfoNode) {
						BrowserUtil.launchBrowser(((ServerInfoNode) o).getServerType().getInfoUrl());
					}
				}
			}
		}

		public void mouseMoved(final MouseEvent e) {
			jtree.setCursor(isHLinkHit(e) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
		}

		private boolean isHLinkHit(MouseEvent mouseevent) {
			TreePath treepath = jtree.getPathForLocation(mouseevent.getX(), mouseevent.getY());
			if (treepath != null) {
				final Object o = treepath.getLastPathComponent();
				if (o instanceof ServerInfoNode) {
					return true;
				}
			}
			return false;
		}
	}

}
