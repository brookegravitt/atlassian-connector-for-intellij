package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.config.serverconfig.model.*;
import com.atlassian.theplugin.idea.config.serverconfig.util.ServerNameUtil;
import com.atlassian.theplugin.ServerType;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Collection;

public class ServerTreePanel extends JPanel implements TreeSelectionListener {

    private JTree serverTree = null;
    private transient PluginConfiguration pluginConfiguration = null;
    private ServerTreeModel model;
    private DefaultMutableTreeNode selectedNode = null;
    private DefaultMutableTreeNode newSelectedNode = null;
    private DefaultMutableTreeNode firstServerNode = null;

    private static final int WIDTH = 150;
    private static final int HEIGHT = 250;
    private static final int VISIBLE_ROW_COUNT = 7;

    public void setModel(ServerTreeModel model) {
        this.model = model;
    }

    public ServerTreePanel() {
        initLayout();
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        add(new JScrollPane(getServerTree()), BorderLayout.CENTER);
    }

//    private void expandAllPaths() {
//        for (int i = 0; i < serverTree.getRowCount(); ++i) {
//                 serverTree.expandRow(i);
//        }
//    }

    private JTree getServerTree() {
        if (serverTree == null) {
            serverTree = new JTree();
            serverTree.setName("Server tree");

            RootNode root = new RootNode();
            model = new ServerTreeModel(root);
            serverTree.setModel(model);

            serverTree.setRootVisible(false);
            serverTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            //serverTree.setExpandsSelectedPaths(true);
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
        ServerBean newServer = new ServerBean();

        Collection<Server> servers = pluginConfiguration.getProductServers(serverType).getServers();
        newServer.setName(ServerNameUtil.suggestNewName(servers));
        pluginConfiguration.getProductServers(serverType).storeServer(newServer);

        ServerNode child = ServerNodeFactory.getServerNode(serverType, newServer);
        ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType, true);
        model.insertNodeInto(child, serverTypeNode, serverTypeNode.getChildCount());
        model.reload();

        TreePath path = new TreePath(child.getPath());
        serverTree.scrollPathToVisible(path);
        serverTree.setSelectionPath(path);
        serverTree.expandPath(path);

        return newServer.getName();
    }

    public void copyServer() {
/*
		ServerBean newServer = new ServerBean();
		newServer.setName(suggestCopyName(ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers()));
		ConfigurationFactory.getConfiguration().getBambooConfiguration().addServer(newServer);
		serverTree.updateUI();
		return newServer.getName();
*/
    }


    public void removeServer() {
        if (selectedNode != null) {
            if (selectedNode instanceof ServerNode) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete the selected server?",
                        "Confirm server delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null);

                if (response != 0) {
                    return;
                }
                TreePath path = new TreePath(((DefaultMutableTreeNode) selectedNode.getParent()).getPath());
                this.pluginConfiguration.getProductServers(
                        ((ServerNode) selectedNode).getServerType()).removeServer(((ServerNode) selectedNode).getServer());
                updateTreeConfiguration();

                serverTree.expandPath(path);
            }
        }
    }

    public void setData(PluginConfiguration aPluginConfiguration) {
        // jgorycki: I assume this method will only be called at the beginning of the dialog's lifecycle.
        // I want to expand all paths in the tree and not select any nodes - hence showing an empty panel
        this.pluginConfiguration = aPluginConfiguration;
        updateTreeConfiguration();
        //expandAllPaths();
        //serverTree.setSelectionPath(null);
        //ConfigPanel.getInstance().getServerConfigPanel().showEmptyPanel();
    }

    private void updateServerTree(ServerType serverType) {
        Collection<Server> servers = pluginConfiguration.getProductServers(serverType).getServers();

        // !servers.isEmpty() because:
        // if server list is empty, don't create server type node,
        // otherwise create node - it would be required
        ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType, !servers.isEmpty());
        if (serverTypeNode != null) {
            serverTypeNode.removeAllChildren();
        }
        model.reload();

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
    }

    private void updateTreeConfiguration() {
        firstServerNode = null;
        ((DefaultMutableTreeNode) model.getRoot()).removeAllChildren();

        for (int i = 0; i < ServerType.values().length; i++) {
            ServerType serverType = ServerType.values()[i];
            updateServerTree(serverType);
        }

        if (newSelectedNode != null) {
            selectedNode = newSelectedNode;
            TreePath path = new TreePath(selectedNode.getPath());
            serverTree.scrollPathToVisible(path);
            serverTree.setSelectionPath(path);
            serverTree.expandPath(path);
        } else {
            selectedNode = null;
            ConfigPanel.getInstance().getServerConfigPanel().showEmptyPanel();
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
                    ConfigPanel.getInstance().storeServer((ServerNode) oldNode);
                }
            }
            selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (selectedNode instanceof ServerNode) {
                ConfigPanel.getInstance().getServerConfigPanel().editServer(
                        ((ServerNode) selectedNode).getServerType(), ((ServerNode) selectedNode).getServer());
            } else if ((selectedNode instanceof ServerTypeNode) && ((ServerTypeNode) selectedNode).getServerType() == ServerType.BAMBOO_SERVER) {
				// bamboo group/general panel
				ConfigPanel.getInstance().getServerConfigPanel().showBambooGeneralPanel();
			} else {
                ConfigPanel.getInstance().getServerConfigPanel().showEmptyPanel();
            }
        } else {
            ConfigPanel.getInstance().getServerConfigPanel().showEmptyPanel();
        }

    }

}


