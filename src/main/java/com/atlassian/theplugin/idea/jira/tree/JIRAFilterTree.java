package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class JIRAFilterTree extends JTree implements JIRAFilterListModelListener {
	private static final ServerTreeRenderer MY_RENDERER = new ServerTreeRenderer();

	private JIRAFilterListModel listModel;
	private DefaultTreeModel treeModel;

	public void setListModel(final JIRAFilterListModel listModel) {
		this.listModel = listModel;
	}

	public void modelChanged(final JIRAFilterListModel listModel) {
		createServerNodes(listModel, (DefaultMutableTreeNode) treeModel.getRoot());
	}

	public JIRAFilterTree(JIRAFilterListModel listModel) {
		final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");

		treeModel = new DefaultTreeModel(rootNode);

		setModel(treeModel);
		setRootVisible(false);
		expandRow(0);
		if (listModel != null) {
			listModel.addModelListener(this);
			createServerNodes(listModel, (DefaultMutableTreeNode) treeModel.getRoot());
			
		}

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setCellRenderer(MY_RENDERER);
	}

	private void createServerNodes(JIRAFilterListModel listModel, DefaultMutableTreeNode rootNode) {
		this.listModel = listModel;
		
		if (listModel == null) {
			return;
		}

		for (JiraServerCfg server : listModel.getJIRAServers()) {
			JIRAServerTreeNode serverNode = new JIRAServerTreeNode(server);
			rootNode.add(serverNode);
		}
	}

	private static class ServerTreeRenderer implements TreeCellRenderer {

		public Component getTreeCellRendererComponent(final JTree jTree, final Object o, final boolean b, final boolean b1,
				final boolean b2, final int i, final boolean b3) {

			if (o instanceof JIRAServerTreeNode) {
				JIRAServerTreeNode node = (JIRAServerTreeNode) o;
				return new JLabel(node.getNodeName());
			} else {
				return new JLabel("Root");
			}
		}
	}


}
