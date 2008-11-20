package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;
import org.jetbrains.annotations.NotNull;

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
	private JIRAFilterListModel listModel;
	private boolean isAlreadyInitialized = false;

	public JIRAFilterTree(@NotNull final JIRAFilterListModel listModel) {
		this.listModel = listModel;

		listModel.addModelListener(this);
		
		setShowsRootHandles(true);
		setRootVisible(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		getSelectionModel().addTreeSelectionListener(treeSelectionListener);
		setCellRenderer(MY_RENDERER);

		reCreateTree(listModel);		

	}

	private void reCreateTree(final JIRAFilterListModel aListModel) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();		
		treeModel = new DefaultTreeModel(rootNode);

		setModel(treeModel);

		if (aListModel != null) {
			createServerNodes(aListModel, (DefaultMutableTreeNode) treeModel.getRoot());
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

	public void modelChanged(JIRAFilterListModel aListModel) {
		removeAll();
		reCreateTree(aListModel);
		expandAll();
		
		//should only be used once during configuration read
		if (!isAlreadyInitialized) {
			setSelectionSavedFilter();
			setSelectionManualFilter();
			isAlreadyInitialized = true;
		}
	}

	public void setSelectionSavedFilter() {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
		if (rootNode == null) {
			return;
		}
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
				JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
				if (node.getJiraServer().equals(listModel.getJiraSelectedServer())) {
					for (int j = 0; j < node.getChildCount(); j++) {
						if (node.getChildAt(j) instanceof JIRASavedFilterTreeNode) {
							JIRASavedFilterTreeNode savedFilterNode = (JIRASavedFilterTreeNode) node.getChildAt(j);
							if (savedFilterNode.getSavedFilter().equals(listModel.getJiraSelectedSavedFilter())) {
								setSelectionPath(new TreePath(savedFilterNode.getPath()));
								break;
							}
						}
					}
				}
			}
		}
	}

	public void setSelectionManualFilter() {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
		if (rootNode == null) {
			return;
		}
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
				JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
				if (node.getJiraServer().equals(listModel.getJiraSelectedServer())) {
					for (int j = 0; j < node.getChildCount(); j++) {
						if (node.getChildAt(j) instanceof JIRAManualFilterTreeNode) {
							JIRAManualFilterTreeNode manualFilterNode = (JIRAManualFilterTreeNode) node.getChildAt(j);
							if (manualFilterNode.getManualFilter().equals(listModel.getJiraSelectedManualFilter())) {
								setSelectionPath(new TreePath(manualFilterNode.getPath()));
								break;
							}
						}
					}
				}
			}
		}

	}

	public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
	}

	public void selectedManualFilter(final JiraServerCfg jiraServer, final java.util.List<JIRAQueryFragment> manualFilter) {
	}

	public void modelFrozen(boolean frozen) {
			this.setEnabled(!frozen);
	}


	private void createServerNodes(JIRAFilterListModel aListModel, DefaultMutableTreeNode rootNode) {

		if (aListModel == null) {
			return;
		}

		for (JiraServerCfg server : aListModel.getJIRAServers()) {
			JIRAServerTreeNode serverNode = new JIRAServerTreeNode(aListModel, server);
			createFilterNodes(server, serverNode, aListModel);
			rootNode.add(serverNode);
		}
	}

	private void createFilterNodes(JiraServerCfg jiraServer, DefaultMutableTreeNode node, JIRAFilterListModel aListModel) {
		if (aListModel != null) {
			for (JIRASavedFilter savedFilter : aListModel.getSavedFilters(jiraServer)) {
				node.add(new JIRASavedFilterTreeNode(aListModel, savedFilter));
			}

			JIRAManualFilter manualFilter = aListModel.getManualFilter(jiraServer);

			node.add(new JIRAManualFilterTreeNode(aListModel, manualFilter));
		}

	}

	private static class ServerTreeRenderer extends DefaultTreeCellRenderer {
		@Override
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
