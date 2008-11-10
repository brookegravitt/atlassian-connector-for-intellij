package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class JIRAFilterTree extends JTree implements JIRAFilterListModelListener {

	private static final ServerTreeRenderer MY_RENDERER = new ServerTreeRenderer();
	private DefaultTreeModel treeModel;

	public JIRAFilterTree(final JIRAFilterListModel listModel) {
		listModel.addModelListener(this);
		reCreateTree(listModel);
	}

	private void reCreateTree(final JIRAFilterListModel listModel) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(rootNode);

		setModel(treeModel);

		setShowsRootHandles(true);
		setRootVisible(false);
		if (listModel != null) {

			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			setCellRenderer(MY_RENDERER);
			getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

				public void valueChanged(final TreeSelectionEvent event) {
					final TreePath selectionPath = getSelectionModel().getSelectionPath();

					if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
						((JIRAAbstractTreeNode) selectionPath.getLastPathComponent()).onSelect();
					}
				}
			});

			createServerNodes(listModel, (DefaultMutableTreeNode) treeModel.getRoot());
		}
		treeModel.nodeStructureChanged(rootNode);
	}

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
		removeAll();
		reCreateTree(listModel);
		expandAll();
	}

	public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
	}

	public void selectedManualFilter(final JiraServerCfg jiraServer, final java.util.List<JIRAQueryFragment> manualFilter) {
	}

	private void createServerNodes(JIRAFilterListModel listModel, DefaultMutableTreeNode rootNode) {

		if (listModel == null) {
			return;
		}

		for (JiraServerCfg server : listModel.getJIRAServers()) {
			JIRAServerTreeNode serverNode = new JIRAServerTreeNode(listModel, server);
			createFilterNodes(server, serverNode, listModel);
			rootNode.add(serverNode);
		}
	}

	private void createFilterNodes(JiraServerCfg jiraServer, DefaultMutableTreeNode node, JIRAFilterListModel listModel) {
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
				return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			}
		}
	}


}
