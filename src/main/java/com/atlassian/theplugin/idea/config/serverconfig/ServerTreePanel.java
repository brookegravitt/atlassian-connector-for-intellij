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
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.serverconfig.model.RootNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerNodeFactory;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerTreeModel;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerTypeNode;
import com.atlassian.theplugin.idea.config.serverconfig.util.ServerNameUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NonNls;
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
import java.util.Collection;

public final class ServerTreePanel extends JPanel implements TreeSelectionListener, DataProvider {

	private JTree serverTree;
	private ServerTreeModel model;
	private DefaultMutableTreeNode selectedNode;
	private boolean forceExpand = true;

	private static final int WIDTH = 150;
	private static final int HEIGHT = 250;
	private static final int VISIBLE_ROW_COUNT = 7;
    private Collection<ServerCfg> servers;

    /**
	 * serverConfigPanel needs to be initialized outside of the constructor to avoid cyclic dependency.
	 * @param serverConfigPanel panel to invoke storeServer() and showEmptyPanel() on.
	 */
	public void setServerConfigPanel(ServerConfigPanel serverConfigPanel) {
		this.serverConfigPanel = serverConfigPanel;
	}

	private ServerConfigPanel serverConfigPanel;

	public ServerTreePanel() {
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
			serverTree.addMouseListener(new MouseAdapter() {
				public static final String TOOLBAR_NAME = "ThePlugin.AddRemoveServerPopup";

				@Override
				public void mousePressed(MouseEvent e) {
					processPopup(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					processPopup(e);
				}

				public void processPopup(MouseEvent e) {
					if (e.isPopupTrigger() == false) {
						return;
					}

					final JTree theTree = (JTree) e.getComponent();

					TreePath path = theTree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						theTree.setSelectionPath(path);
					}
//					Object o = path.getLastPathComponent();
//					if (o instanceof ServerNode) {
						ActionGroup menu = (ActionGroup) ActionManager.getInstance().getAction(TOOLBAR_NAME);
						if (menu == null) {
							return;
						}
						ActionManager.getInstance().createActionPopupMenu(toString(), menu)
								.getComponent().show(e.getComponent(), e.getX(), e.getY());
//					}
				}
			});
			

			serverTree.setCellRenderer(new ServerTreeRenderer());
		}
		return serverTree;
	}

	@Override
    public void setEnabled(boolean b) {
		super.setEnabled(b);
		getServerTree().setEnabled(b);
	}

	public String addServer(ServerType serverType) {


        String name = ServerNameUtil.suggestNewName(servers);
        ServerCfg newServer = createNewServer(serverType, name);

        servers.add(newServer);

        ServerNode child = ServerNodeFactory.getServerNode(newServer);
		ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType, true);
		model.insertNodeInto(child, serverTypeNode, serverTypeNode.getChildCount());

		TreePath path = new TreePath(child.getPath());
		serverTree.scrollPathToVisible(path);
		serverTree.setSelectionPath(path);
		serverTree.expandPath(path);

		return newServer.getName();
	}

    private ServerCfg createNewServer(final ServerType serverType, final String name) {
        ServerId id = new ServerId();
			// CHECKSTYLE:OFF
		switch (serverType) {
			// CHECKSTYLE:ON
			case BAMBOO_SERVER:
				return new BambooServerCfg(true, name, id);
			case CRUCIBLE_SERVER:
				return new CrucibleServerCfg(name, id);
			case JIRA_SERVER:
				return new JiraServerCfg(name, id);
			case FISHEYE_SERVER:
				return new FishEyeServerCfg(name, id);
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
			    ServerTypeNode serverTypeNode = model.getServerTypeNode(server.getServerType(), true);

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
			}
		} else {
			serverConfigPanel.showEmptyPanel();
		}
	}


	private ServerCfg getSelectedServer() {
		if (selectedNode instanceof ServerNode) {
			return ((ServerNode) selectedNode).getServer();
		}
		return null;
	}

	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.SERVER)) {
			return getSelectedServer();
		} else if (dataId.equals(Constants.SERVER_TYPE)) {
			if (selectedNode instanceof ServerTypeNode) {
				final ServerTypeNode serverTypeNode = (ServerTypeNode) selectedNode;
				return serverTypeNode.getServerType();
			}
		}
		return null;
	}
}
