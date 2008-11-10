package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;

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
	private FilterTreeSelectionListener treeSelectionListener = new FilterTreeSelectionListener();

	public JIRAFilterTree(final JIRAFilterListModel listModel) {
		listModel.addModelListener(this);
		
		setShowsRootHandles(true);
		setRootVisible(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		getSelectionModel().addTreeSelectionListener(treeSelectionListener);
		setCellRenderer(MY_RENDERER);

		reCreateTree(listModel);
	}

	private void reCreateTree(final JIRAFilterListModel listModel) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();		
		treeModel = new DefaultTreeModel(rootNode);

		setModel(treeModel);

		if (listModel != null) {
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
		
		JIRAManualFilter manualFilter = listModel.getManualFilter(jiraServer);

		node.add(new JIRAManualFilterTreeNode(listModel, manualFilter));

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

	class FilterTreeSelectionListener implements TreeSelectionListener {

		public void valueChanged(final TreeSelectionEvent event) {
			final TreePath selectionPath = getSelectionModel().getSelectionPath();

			if (selectionPath != null
					&& selectionPath.getLastPathComponent() != null
					&& selectionPath.getLastPathComponent() instanceof JIRAAbstractTreeNode) {

				((JIRAAbstractTreeNode) selectionPath.getLastPathComponent()).onSelect();
			}
		}
	}


}
