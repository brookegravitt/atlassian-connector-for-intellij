package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.*;
import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: pmaruszak
 */
public class JIRAFilterTree extends JTree {

	private static final JIRAFilterTreeRenderer MY_RENDERER = new JIRAFilterTreeRenderer();
	private JiraWorkspaceConfiguration jiraProjectConfiguration;
	//	private JIRAFilterListModel listModel;
	private boolean isAlreadyInitialized = false;
	private Collection<JiraFilterTreeSelectionListener> selectionListeners = new HashSet<JiraFilterTreeSelectionListener>();
	private LocalTreeSelectionListener localSelectionListener = new LocalTreeSelectionListener();

	public JIRAFilterTree(@NotNull final JiraWorkspaceConfiguration jiraProjectConfiguration,
			@NotNull final JIRAFilterListModel listModel) {

		this.jiraProjectConfiguration = jiraProjectConfiguration;
//		this.listModel = listModel;

		listModel.addModelListener(new LocalFilterListModelListener());

		setShowsRootHandles(true);
		setRootVisible(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		getSelectionModel().addTreeSelectionListener(localSelectionListener);
		setCellRenderer(MY_RENDERER);

		reCreateTree(listModel, true);

		listModel.addFrozenModelListener(new FrozenModelListener() {

			public void modelFrozen(FrozenModel model, boolean frozen) {
				JIRAFilterTree.this.setEnabled(!frozen);
			}
		});
	}

	public JiraServerCfg getSelectedServer() {
		TreePath selectionPath = getSelectionModel().getSelectionPath();
		if (selectionPath != null) {
			if (selectionPath.getLastPathComponent() instanceof JIRAServerTreeNode) {
				return ((JIRAServerTreeNode) (selectionPath.getLastPathComponent())).getJiraServer();
			} else {
				return ((JIRAServerTreeNode) (
						(DefaultMutableTreeNode) selectionPath.getLastPathComponent()).getParent()).getJiraServer();
			}
		}
		return null;
	}

	public JIRAManualFilter getSelectedManualFilter() {
		TreePath selectionPath = getSelectionModel().getSelectionPath();
		if (selectionPath != null && selectionPath.getLastPathComponent() instanceof JIRAManualFilterTreeNode) {
			return ((JIRAManualFilterTreeNode) selectionPath.getLastPathComponent()).getManualFilter();
		}
		return null;
	}

	public JIRASavedFilter getSelectedSavedFilter() {
		TreePath selectionPath = getSelectionModel().getSelectionPath();
		if (selectionPath != null && selectionPath.getLastPathComponent() instanceof JIRASavedFilterTreeNode) {
			return ((JIRASavedFilterTreeNode) selectionPath.getLastPathComponent()).getSavedFilter();
		}
		return null;
	}

	private void reCreateTree(final JIRAFilterListModel aListModel, final boolean fireSelectionChange) {
		// off selection listener
		getSelectionModel().removeTreeSelectionListener(localSelectionListener);
		DefaultTreeModel treeModel;
		removeAll();
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(rootNode);

		setModel(treeModel);

		if (aListModel != null) {
			createServerNodes(aListModel, (DefaultMutableTreeNode) treeModel.getRoot());
		}
		treeModel.nodeStructureChanged((DefaultMutableTreeNode) treeModel.getRoot());

		if (fireSelectionChange) {
			// on selection listener
			getSelectionModel().addTreeSelectionListener(localSelectionListener);
			setSelectionFilter(jiraProjectConfiguration.getView().getViewFilterId(),
					jiraProjectConfiguration.getView().getViewServerId());
		} else {
			setSelectionFilter(jiraProjectConfiguration.getView().getViewFilterId(),
					jiraProjectConfiguration.getView().getViewServerId());
			getSelectionModel().addTreeSelectionListener(localSelectionListener);
		}

	}

	private void setSelectionFilter(final String viewFilterId, final String viewServerId) {
		boolean filterFound = false;
		if (JiraFilterConfigurationBean.MANUAL_FILTER_LABEL.equals(viewFilterId)) {
			filterFound = setSelectionManualFilter(viewServerId);
		} else if (viewFilterId != null && viewFilterId.length() > 0) {
			try {
				filterFound = setSelectionSavedFilter(Long.parseLong(viewFilterId), viewServerId);
			} catch (NumberFormatException e) {
				PluginUtil.getLogger().warn("Invalid saved filter id (should be long): " + viewServerId, e);
			}
		}
		if (!filterFound) {
			localSelectionListener.fireSelectionCleared();
		}
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


	public boolean setSelectionSavedFilter(final long savedFilterId, final String serverId) {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
		if (rootNode == null) {
			return false;
		}
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
				JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
				if (node.getJiraServer().getServerId().toString().equals(serverId)) {
					for (int j = 0; j < node.getChildCount(); j++) {
						if (node.getChildAt(j) instanceof JIRASavedFilterTreeNode) {
							JIRASavedFilterTreeNode savedFilterNode = (JIRASavedFilterTreeNode) node.getChildAt(j);
							if (savedFilterNode.getSavedFilter().getId() == savedFilterId) {
								setSelectionPath(new TreePath(savedFilterNode.getPath()));
								scrollPathToVisible(new TreePath(savedFilterNode.getPath()));
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public boolean setSelectionManualFilter(final String serverId) {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
		if (rootNode == null) {
			return false;
		}
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
				JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
				if (node.getJiraServer().getServerId().toString().equals(serverId)) {
					for (int j = 0; j < node.getChildCount(); j++) {
						if (node.getChildAt(j) instanceof JIRAManualFilterTreeNode) {
							JIRAManualFilterTreeNode manualFilterNode = (JIRAManualFilterTreeNode) node.getChildAt(j);

							// single manual filter support
//							if (manualFilterNode.getManualFilter().equals(manualFilter)) {
							setSelectionPath(new TreePath(manualFilterNode.getPath()));
							scrollPathToVisible(new TreePath(manualFilterNode.getPath()));
							return true;
//							}
						}
					}
				}
			}
		}
		return false;
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
				node.add(new JIRASavedFilterTreeNode(savedFilter, jiraServer));
			}

			JIRAManualFilter manualFilter = aListModel.getManualFilter(jiraServer);

			node.add(new JIRAManualFilterTreeNode(manualFilter, jiraServer));
		}

	}

	public void addSelectionListener(final JiraFilterTreeSelectionListener jiraFilterTreeSelectionListener) {
		selectionListeners.add(jiraFilterTreeSelectionListener);
	}

	public void removeSelectionListener(final JiraFilterTreeSelectionListener jiraFilterTreeSelectionListener) {
		selectionListeners.remove(jiraFilterTreeSelectionListener);
	}

	private class LocalTreeSelectionListener implements TreeSelectionListener {
		private JIRAManualFilter prevManualFilter = null;
		private JIRASavedFilter prevSavedFilter = null;

		public final void valueChanged(final TreeSelectionEvent event) {

			JIRAManualFilter manualFilter = getSelectedManualFilter();
			JIRASavedFilter savedFilter = getSelectedSavedFilter();
			JiraServerCfg serverCfg = getSelectedServer();

			if (manualFilter != null) {
				prevManualFilter = manualFilter;
				prevSavedFilter = null;
				fireSelectedManualFilterNode(manualFilter, serverCfg);
			} else if (savedFilter != null) {
				prevSavedFilter = savedFilter;
				prevManualFilter = null;
				fireSelectedSavedFilterNode(savedFilter, serverCfg);
			} else if (serverCfg != null) {
				// server selected: do not fire notification (we must ignore that action)
				getSelectionModel().removeTreeSelectionListener(localSelectionListener);

				// remove server selection
				clearSelection();

				// restore previous selection
				if (prevManualFilter != null) {
					setSelectionManualFilter(serverCfg.getServerId().toString());
				} else if (prevSavedFilter != null) {
					setSelectionSavedFilter(prevSavedFilter.getId(), serverCfg.getServerId().toString());
				}

				getSelectionModel().addTreeSelectionListener(localSelectionListener);

			} else {
				// all nodes unselected
				prevSavedFilter = null;
				prevManualFilter = null;
				fireSelectionCleared();
			}
		}

		public void fireSelectionCleared() {
			for (JiraFilterTreeSelectionListener listener : selectionListeners) {
				listener.selectionCleared();
			}
		}

		private void fireSelectedSavedFilterNode(final JIRASavedFilter savedFilter, final JiraServerCfg serverCfg) {
			for (JiraFilterTreeSelectionListener listener : selectionListeners) {
				listener.selectedSavedFilterNode(savedFilter, serverCfg);
			}
		}

		private void fireSelectedManualFilterNode(final JIRAManualFilter manualFilter, final JiraServerCfg serverCfg) {
			for (JiraFilterTreeSelectionListener listener : selectionListeners) {
				listener.selectedManualFilterNode(manualFilter, serverCfg);
			}
		}

	}

	private class LocalFilterListModelListener implements JIRAFilterListModelListener {
		public void modelChanged(JIRAFilterListModel aListModel) {
			reCreateTree(aListModel, true);
			expandAll();

			//should only be used once during configuration read
			if (!isAlreadyInitialized) {
//			setSelectionSavedFilter();
//			setSelectionManualFilter();
				isAlreadyInitialized = true;
			}
		}

		public void manualFilterChanged(final JIRAManualFilter manualFilter, final JiraServerCfg jiraServer) {
			// we don't care about changes in manual filter
		}

		public void serverRemoved(final JIRAFilterListModel jiraFilterListModel) {
			reCreateTree(jiraFilterListModel, false);
			expandAll();

			//should only be used once during configuration read
			if (!isAlreadyInitialized) {
//			setSelectionSavedFilter();
//			setSelectionManualFilter();
				isAlreadyInitialized = true;
			}
		}

	}
}



