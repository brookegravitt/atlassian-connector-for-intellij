package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.ui.tree.AbstractTree;
import com.atlassian.theplugin.jira.model.*;
import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

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
public class JIRAFilterTree extends AbstractTree {

	private static final JIRAFilterTreeRenderer MY_RENDERER = new JIRAFilterTreeRenderer();
	private JiraWorkspaceConfiguration jiraWorkspaceConfiguration;
	//	private JIRAFilterListModel listModel;
	private boolean isAlreadyInitialized = false;
	private Collection<JiraFilterTreeSelectionListener> selectionListeners = new HashSet<JiraFilterTreeSelectionListener>();
	private LocalTreeSelectionListener localSelectionListener = new LocalTreeSelectionListener();

	public JIRAFilterTree(@NotNull final JiraWorkspaceConfiguration jiraWorkspaceConfiguration,
			@NotNull final JIRAFilterListModel listModel) {

		this.jiraWorkspaceConfiguration = jiraWorkspaceConfiguration;
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

	public ServerData getSelectedServer() {
		TreePath selectionPath = getSelectionModel().getSelectionPath();
		if (selectionPath != null) {
			if (selectionPath.getLastPathComponent() instanceof JIRAServerTreeNode) {
				return ((JIRAServerTreeNode) (selectionPath.getLastPathComponent())).getJiraServer();
			} else if (selectionPath.getLastPathComponent() instanceof JIRASavedFilterTreeNode
					|| selectionPath.getLastPathComponent() instanceof JIRAManualFilterTreeNode) {
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

	public boolean isRecentlyOpenSelected() {
		TreePath selectionPath = getSelectionModel().getSelectionPath();
		if (selectionPath != null && selectionPath.getLastPathComponent() instanceof JiraRecentlyOpenTreeNode) {
			return true;
		}

		return false;
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
		rootNode.add(new JiraRecentlyOpenTreeNode());
		treeModel.nodeStructureChanged((DefaultMutableTreeNode) treeModel.getRoot());

		if (fireSelectionChange) {
			// on selection listener
			getSelectionModel().addTreeSelectionListener(localSelectionListener);
			setSelectionFilter(jiraWorkspaceConfiguration.getView().getViewFilterId(),
					jiraWorkspaceConfiguration.getView().getViewServerIdd());
		} else {
			setSelectionFilter(jiraWorkspaceConfiguration.getView().getViewFilterId(),
					jiraWorkspaceConfiguration.getView().getViewServerIdd());
			getSelectionModel().addTreeSelectionListener(localSelectionListener);
		}

	}

	private void setSelectionFilter(final String viewFilterId, final ServerId viewServerId) {
		boolean filterFound = false;
		if (JiraFilterConfigurationBean.MANUAL_FILTER.equals(viewFilterId)) {
			filterFound = setSelectionManualFilter(viewServerId);
		} else if (JiraFilterConfigurationBean.RECENTLY_OPEN_FILTER.equals(viewFilterId)) {
			filterFound = setSelectionRecentlyOpen();
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

	public boolean setSelectionSavedFilter(final long savedFilterId, final ServerId serverId) {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
		if (rootNode == null) {
			return false;
		}
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
				JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
				if (node.getJiraServer().getServerId().equals(serverId)) {
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

	public boolean setSelectionManualFilter(final ServerId serverId) {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
		if (rootNode == null) {
			return false;
		}
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
				JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
				if (node.getJiraServer().getServerId().equals(serverId)) {
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


	private boolean setSelectionRecentlyOpen() {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
		if (rootNode == null) {
			return false;
		}
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i) instanceof JiraRecentlyOpenTreeNode) {
				JiraRecentlyOpenTreeNode node = (JiraRecentlyOpenTreeNode) rootNode.getChildAt(i);
				setSelectionPath(new TreePath(node.getPath()));
				scrollPathToVisible(new TreePath(node.getPath()));
				return true;
			}
		}
		return false;
	}

	private void createServerNodes(JIRAFilterListModel aListModel, DefaultMutableTreeNode rootNode) {

		if (aListModel == null) {
			return;
		}

//		List<JiraServerCfg> servers = aListModel.getJIRAServers();
//		Collections.sort(servers);

		for (ServerData server : aListModel.getJIRAServers()) {
			JIRAServerTreeNode serverNode = new JIRAServerTreeNode(server);
			createFilterNodes(server, serverNode, aListModel);
			rootNode.add(serverNode);
		}
	}

	private void createFilterNodes(ServerData jiraServer, DefaultMutableTreeNode node, JIRAFilterListModel aListModel) {
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
		private ServerData prevServer = null;
		private boolean prevRecentlyOpen = false;

		public final void valueChanged(final TreeSelectionEvent event) {

			JIRAManualFilter manualFilter = getSelectedManualFilter();
			JIRASavedFilter savedFilter = getSelectedSavedFilter();
			ServerData serverCfg = getSelectedServer();
			boolean recentlyOpenSelected = isRecentlyOpenSelected();


			if (manualFilter != null) {
				prevManualFilter = manualFilter;
				prevSavedFilter = null;
				prevRecentlyOpen = false;
				prevServer = serverCfg;
				fireSelectedManualFilterNode(manualFilter, serverCfg);
			} else if (savedFilter != null) {
				prevSavedFilter = savedFilter;
				prevManualFilter = null;
				prevRecentlyOpen = false;
				prevServer = serverCfg;
				fireSelectedSavedFilterNode(savedFilter, serverCfg);
			} else if (serverCfg != null) {
				// server selected: do not fire notification (we must ignore that action)
				getSelectionModel().removeTreeSelectionListener(localSelectionListener);

				// remove server selection
				clearSelection();

				// restore previous selection
				if (prevManualFilter != null) {
					setSelectionManualFilter(prevServer.getServerId());
				} else if (prevSavedFilter != null) {
					setSelectionSavedFilter(prevSavedFilter.getId(), prevServer.getServerId());
				} else if (prevRecentlyOpen) {
					setSelectionRecentlyOpen();
				}

				getSelectionModel().addTreeSelectionListener(localSelectionListener);

			} else if (recentlyOpenSelected) {
				prevSavedFilter = null;
				prevManualFilter = null;
				prevRecentlyOpen = true;
				fireSelectedRecentlyOpenNode();
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

		private void fireSelectedSavedFilterNode(final JIRASavedFilter savedFilter, final ServerData serverCfg) {
			for (JiraFilterTreeSelectionListener listener : selectionListeners) {
				listener.selectedSavedFilterNode(savedFilter, serverCfg);
			}
		}

		private void fireSelectedManualFilterNode(final JIRAManualFilter manualFilter, final ServerData serverCfg) {
			for (JiraFilterTreeSelectionListener listener : selectionListeners) {
				listener.selectedManualFilterNode(manualFilter, serverCfg);
			}
		}

		private void fireSelectedRecentlyOpenNode() {
			for (JiraFilterTreeSelectionListener listener : selectionListeners) {
				listener.selectedRecentlyOpenNode();
			}
		}

	}


	private class LocalFilterListModelListener implements JIRAFilterListModelListener {
		public void modelChanged(JIRAFilterListModel aListModel) {
			rebuildTree(aListModel, true);
		}

		public void manualFilterChanged(final JIRAManualFilter manualFilter, final ServerData jiraServer) {
			// we don't care about changes in manual filter
		}

		public void serverRemoved(final JIRAFilterListModel jiraFilterListModel) {
			rebuildTree(jiraFilterListModel, false);
		}

		public void serverAdded(final JIRAFilterListModel jiraFilterListModel) {
			rebuildTree(jiraFilterListModel, false);
		}

		public void serverNameChanged(final JIRAFilterListModel jiraFilterListModel) {
			rebuildTree(jiraFilterListModel, false);
		}

		private void rebuildTree(final JIRAFilterListModel jiraFilterListModel, boolean fireSelectionChange) {
			reCreateTree(jiraFilterListModel, fireSelectionChange);
			expandTree();

			//should only be used once during configuration read
			if (!isAlreadyInitialized) {
				isAlreadyInitialized = true;
			}
		}
	}
}



