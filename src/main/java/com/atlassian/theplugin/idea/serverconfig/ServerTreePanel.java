package com.atlassian.theplugin.idea.serverconfig;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.idea.serverconfig.model.*;
import com.atlassian.theplugin.idea.serverconfig.util.ServerNameUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

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

	public void setModel(ServerTreeModel model) {
		this.model = model;
	}

	public ServerTreePanel() {
		initLayout();
	}

	private void initLayout() {
		setLayout(new BorderLayout());
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
			serverTree.setVisibleRowCount(7);
			serverTree.addTreeSelectionListener(this);
		}
		return serverTree;
	}

	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getServerTree().setEnabled(b);
	}

	public String addBambooServer() {
		ServerBean newServer = new ServerBean();
		newServer.setName(ServerNameUtil.suggestNewName(pluginConfiguration.getBambooConfiguration().getServers()));
		pluginConfiguration.getBambooConfiguration().storeServer(newServer);

		BambooServerNode child = new BambooServerNode(newServer);
		ServerTypeNode serverType = model.getServerTypeNode(ServerType.BAMBOO_SERVER);
		model.insertNodeInto(child, serverType, serverType.getChildCount());
		model.reload();

		TreePath path = new TreePath(child.getPath());
		serverTree.scrollPathToVisible(path);
		serverTree.setSelectionPath(path);

		return newServer.getName();
	}

	public String addCrucibleServer() {
		return null;
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
			if (selectedNode instanceof BambooServerNode) {
				int response = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to delete the selected server?",
						"Confirm server delete",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null);

				if (response != 0) {
					return;
				}
				this.pluginConfiguration.getBambooConfiguration().removeServer(((BambooServerNode) selectedNode).getServer());
				updateTreeConfiguration();
			}
		}
	}

	public void setData(PluginConfiguration aPluginConfiguration) {
		this.pluginConfiguration = aPluginConfiguration;
		updateTreeConfiguration();
	}

	private void updateTreeConfiguration() {
		ServerTypeNode serverType = model.getServerTypeNode(ServerType.BAMBOO_SERVER);
		serverType.removeAllChildren();
		model.reload();

		DefaultMutableTreeNode newSelectedNode = null;
		for (Server server : pluginConfiguration.getBambooConfiguration().getServers()) {
			BambooServerNode child = new BambooServerNode((ServerBean)server);
			model.insertNodeInto(child, serverType, serverType.getChildCount());

			if (selectedNode != null && selectedNode instanceof BambooServerNode) {
				if (child.getServer().equals(((BambooServerNode)selectedNode).getServer())) {
					newSelectedNode = child;
				}
			}
		}

		if (newSelectedNode != null) {
			selectedNode = newSelectedNode;
			TreePath path = new TreePath(selectedNode.getPath());
			serverTree.scrollPathToVisible(path);
			serverTree.setSelectionPath(path);
		} else {
			selectedNode = null;
			ServerConfigPanel.getInstance().showEmptyPanel();
		}


	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();

		if (path != null) {
			TreePath oldPath = e.getOldLeadSelectionPath();
			if (oldPath != null) {
				DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) oldPath.getLastPathComponent();
				if (oldNode != null && oldNode instanceof BambooServerNode) {
					ServerConfigPanel.getInstance().storeBambooServer(((BambooServerNode) oldNode).getServer());
//					pluginConfiguration.getBambooConfiguration().storeServer(((BambooServerNode) oldNode).getServer());
				}
			}
			selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (selectedNode instanceof BambooServerNode) {
				ServerConfigPanel.getInstance().editBambooServer(((BambooServerNode) selectedNode).getServer());
			} else {
				ServerConfigPanel.getInstance().showEmptyPanel();
			}
		} else {
			ServerConfigPanel.getInstance().showEmptyPanel();
		}

	}
}


