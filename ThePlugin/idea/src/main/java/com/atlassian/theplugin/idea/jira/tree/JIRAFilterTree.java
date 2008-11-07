package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class JIRAFilterTree extends JTree implements JIRAFilterListModelListener {

	public JIRAFilterTree(final JIRAFilterListModel listModel) {
		final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
		treeModel = new DefaultTreeModel(rootNode);

		setModel(treeModel);
		setShowsRootHandles(true);
		setRootVisible(false);
		if (listModel != null) {
			listModel.addModelListener(this);
			createServerNodes(listModel, (DefaultMutableTreeNode) treeModel.getRoot());
			treeModel.nodeStructureChanged(rootNode);

		}

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setCellRenderer(MY_RENDERER);
		expandAll();
		getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(final TreeSelectionEvent event) {
				((JIRAAbstractTreeNode) getSelectionModel().getSelectionPath().getLastPathComponent()).onSelect();
			}
		});
	}

	private static final ServerTreeRenderer MY_RENDERER = new ServerTreeRenderer();

	//private JIRAFilterListModel listModel;
	private DefaultTreeModel treeModel;
//	public void setListModel(final JIRAFilterListModel listModel) {

//	}

	public void expandAll() {
		for (int i = 0; i < this.getRowCount(); i++) {
			this.expandRow(i);
		}
	}

	public void collapseAll() {
		for (int i = 0; i < this.getRowCount(); i++) {
			this.collapseRow(i);
		}
	}

	public void modelChanged(JIRAFilterListModel listModel) {
		createServerNodes(listModel, (DefaultMutableTreeNode) treeModel.getRoot());
		treeModel.nodeStructureChanged((DefaultMutableTreeNode) treeModel.getRoot());
		expandAll();
	}

	public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void selectedManualFilter(final JiraServerCfg jiraServer,
	                                 final java.util.List<JIRAQueryFragment> manualFilter) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private void createServerNodes(JIRAFilterListModel listModel, DefaultMutableTreeNode rootNode) {

		if (listModel == null) {
			return;
		}

		for (JiraServerCfg server : listModel.getJIRAServers()) {
			JIRAServerTreeNode serverNode = new JIRAServerTreeNode(listModel, server);
			rootNode.add(serverNode);
			createFilterNodes(server, serverNode, listModel);
		}
	}


	private void createFilterNodes(JiraServerCfg jiraServer, DefaultMutableTreeNode node,
	                               JIRAFilterListModel listModel) {
		for (JIRASavedFilter savedFilter : listModel.getSavedFilters(jiraServer)) {
			node.add(new JIRASavedFilterTreeNode(listModel, savedFilter));
		}
		//@todo refactor to separate class
		node.add(new JIRAManualFilterTreeNode("Undefined manual filter"));

	}

	private static class ServerTreeRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
				boolean expanded, boolean leaf, int row, boolean hasFocus) {

			if (value instanceof JIRAAbstractTreeNode) {
				JIRAAbstractTreeNode node = (JIRAAbstractTreeNode) value;
				return node.getRenderer(null, selected, expanded, hasFocus);
			} else {
				return new JLabel("Root");
			}
		}
	}


}
