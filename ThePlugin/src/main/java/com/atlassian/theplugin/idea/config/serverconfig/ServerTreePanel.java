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

/**
 * User: mwent
 * Date: 2008-01-26
 * Time: 13:01:18
 */
public class ServerTreePanel extends JPanel implements TreeSelectionListener {

    private JTree serverTree = null;
    private PluginConfiguration pluginConfiguration = null;
    private ServerTreeModel model;
    private DefaultMutableTreeNode selectedNode = null;
    private DefaultMutableTreeNode newSelectedNode = null;
    private DefaultMutableTreeNode firstServerNode = null;

    public void setModel(ServerTreeModel model) {
        this.model = model;
    }

    public ServerTreePanel() {
        initLayout();
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(150, 250));
        add(new JScrollPane(getServerTree()), BorderLayout.CENTER);
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
            serverTree.setExpandsSelectedPaths(true);
            serverTree.setVisibleRowCount(7);

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
        ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType);
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
                this.pluginConfiguration.getProductServers(((ServerNode) selectedNode).getServerType()).removeServer(((ServerNode) selectedNode).getServer());
                updateTreeConfiguration();

                serverTree.expandPath(path);
            }
        }
    }

    public void setData(PluginConfiguration aPluginConfiguration) {
        this.pluginConfiguration = aPluginConfiguration;
        updateTreeConfiguration();
    }

    private void updateServerTree(ServerType serverType) {
        Collection<Server> servers = pluginConfiguration.getProductServers(serverType).getServers();

        ServerTypeNode serverTypeNode = model.getServerTypeNode(serverType);
        serverTypeNode.removeAllChildren();
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
        updateServerTree(ServerType.BAMBOO_SERVER);
        updateServerTree(ServerType.CRUCIBLE_SERVER);

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
                ConfigPanel.getInstance().getServerConfigPanel().editServer(((ServerNode) selectedNode).getServerType(), ((ServerNode) selectedNode).getServer());
            } else {
                ConfigPanel.getInstance().getServerConfigPanel().showEmptyPanel();
            }
        } else {
            ConfigPanel.getInstance().getServerConfigPanel().showEmptyPanel();
        }

    }
}


