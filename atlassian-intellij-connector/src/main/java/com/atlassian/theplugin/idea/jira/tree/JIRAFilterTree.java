package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.ui.tree.AbstractTree;
import com.atlassian.theplugin.jira.model.FrozenModel;
import com.atlassian.theplugin.jira.model.FrozenModelListener;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;
import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: pmaruszak
 */
public class JIRAFilterTree extends AbstractTree {

    private static final JIRAFilterTreeRenderer MY_RENDERER = new JIRAFilterTreeRenderer();
    private final ProjectCfgManager projectCfgManager;
    private JiraWorkspaceConfiguration jiraWorkspaceConfiguration;
    private final Project project;
    //	private JIRAFilterListModel listModel;
    private boolean isAlreadyInitialized = false;
    private Collection<JiraFilterTreeSelectionListener> selectionListeners = new HashSet<JiraFilterTreeSelectionListener>();
    private LocalTreeSelectionListener localSelectionListener = new LocalTreeSelectionListener();
    private JiraServerData lastSelectedServer;

    public JIRAFilterTree(@NotNull ProjectCfgManager projectCfgManager,
                          @NotNull final JiraWorkspaceConfiguration jiraWorkspaceConfiguration,
                          @NotNull final JIRAFilterListModel listModel,
                          @NotNull Project project) {
        this.projectCfgManager = projectCfgManager;

        this.jiraWorkspaceConfiguration = jiraWorkspaceConfiguration;
        this.project = project;

        listModel.addModelListener(new LocalFilterListModelListener());
        setShowsRootHandles(true);
        setRootVisible(false);
        /*setSelectionModel(new CustomSingleTreeSelectionModel());*/
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        getSelectionModel().addTreeSelectionListener(localSelectionListener);
        setCellRenderer(MY_RENDERER);

        reCreateTree(listModel, true);

        listModel.addFrozenModelListener(new FrozenModelListener() {

            public void modelFrozen(FrozenModel model, final boolean frozen) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JIRAFilterTree.this.setEnabled(!frozen);
                    }
                });
            }
        });
    }

    @Nullable
    public JiraServerData getSelectedServer() {
        TreePath selectionPath = getSelectionModel().getSelectionPath();

        if (selectionPath != null) {
            if (selectionPath.getLastPathComponent() instanceof JIRAServerTreeNode) {
                lastSelectedServer = ((JIRAServerTreeNode) (selectionPath.getLastPathComponent())).getJiraServer();
            } else if (selectionPath.getLastPathComponent() instanceof JIRASavedFilterTreeNode
                    || selectionPath.getLastPathComponent() instanceof JIRAManualFilterTreeNode
                    || selectionPath.getLastPathComponent() instanceof JiraPresetFilterTreeNode) {
                lastSelectedServer = ((JIRAServerTreeNode) (
                        (DefaultMutableTreeNode) selectionPath.getLastPathComponent()).getParent()).getJiraServer();

            }

        }
       JiraServerData selectedServer = null;
       if (lastSelectedServer != null) {
           selectedServer = projectCfgManager.getJiraServerr(lastSelectedServer.getServerId());
       }
        
       return (selectedServer != null && selectedServer.isEnabled() ? selectedServer : null);
    }

    @Nullable
    public DefaultMutableTreeNode getSelectedNode() {
        TreePath selectionPath = getSelectionModel().getSelectionPath();
        if (selectionPath != null) {
            return (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        }

        return null;
    }

    public JiraPresetFilter getSelectedPresetFilter() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode instanceof JiraPresetFilterTreeNode) {
            return ((JiraPresetFilterTreeNode) selectedNode).getPresetFilter();
        } 
        return null;
    }

    public JiraCustomFilter getSelectedManualFilter() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode instanceof JIRAManualFilterTreeNode) {
            return ((JIRAManualFilterTreeNode) selectedNode).getManualFilter();
        }
        return null;
    }

    public JIRASavedFilter getSelectedSavedFilter() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode != null && selectedNode instanceof JIRASavedFilterTreeNode) {
            return ((JIRASavedFilterTreeNode) selectedNode).getSavedFilter();
        }
        return null;
    }

    public boolean isRecentlyOpenSelected() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        return selectedNode != null && selectedNode instanceof JiraRecentlyOpenTreeNode;

    }

       public boolean isPresetFilterGroupNodeSelected() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        return selectedNode != null && selectedNode instanceof JiraPresetFiltersGroupTreeNode;
    }
    public boolean isServerNodeSelected() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        return selectedNode != null && selectedNode instanceof JIRAServerTreeNode;
    }

    public void reCreateTree(final JIRAFilterListModel aListModel, final boolean fireSelectionChange) {
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
            setSelectionFilter(jiraWorkspaceConfiguration.getView().getViewFilterType(),
                    jiraWorkspaceConfiguration.getView().getViewFilterId(),
                    jiraWorkspaceConfiguration.getView().getViewServerIdd());
        } else {
            setSelectionFilter(jiraWorkspaceConfiguration.getView().getViewFilterType(),
                    jiraWorkspaceConfiguration.getView().getViewFilterId(),
                    jiraWorkspaceConfiguration.getView().getViewServerIdd());
            getSelectionModel().addTreeSelectionListener(localSelectionListener);
        }
    }

    private void setSelectionFilter(final String filterType, final String viewFilterId, final ServerId viewServerId) {
        boolean filterFound = false;
        if (JiraFilterConfigurationBean.PRESET_FILTER.equals(filterType)) {
            filterFound = setSelectionPresetFilter(viewServerId, viewFilterId);
        } else if (JiraFilterConfigurationBean.MANUAL_FILTER.equals(filterType)) {
            filterFound = setSelectionManualFilter(viewServerId, viewFilterId);
        } else if (JiraWorkspaceConfiguration.RECENTLY_OPEN_FILTER_ID.equals(filterType)) {
            filterFound = setSelectionRecentlyOpen();
        } else if (JiraFilterConfigurationBean.SAVED_FILTER.equals(filterType)
                && viewFilterId != null && viewFilterId.length() > 0) {
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
                if (node.getJiraServer() != null && node.getJiraServer().getServerId().equals(serverId)) {
                    for (int j = 0; j < node.getChildCount(); j++) {
                        if (node.getChildAt(j) instanceof JiraSavedFiltersGroupTreeNode) {
                            JiraSavedFiltersGroupTreeNode sfNode = (JiraSavedFiltersGroupTreeNode) node.getChildAt(j);
                            for (int k = 0; k < sfNode.getChildCount(); k++) {
                                if (sfNode.getChildAt(k) instanceof JIRASavedFilterTreeNode) {
                                    JIRASavedFilterTreeNode savedFilterNode = (JIRASavedFilterTreeNode) sfNode.getChildAt(k);
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
            }
        }
        return false;
    }

    private boolean setSelectionPresetFilter(ServerId serverId, String filterClass) {
        DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
        if (rootNode == null) {
            return false;
        }
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
                JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
                if (node.getJiraServer() != null && node.getJiraServer().getServerId().equals(serverId)) {
                    for (int j = 0; j < node.getChildCount(); j++) {
                        if (node.getChildAt(j) instanceof JiraPresetFiltersGroupTreeNode) {
                            JiraPresetFiltersGroupTreeNode pfNode = (JiraPresetFiltersGroupTreeNode) node.getChildAt(j);
                            for (int k = 0; k < pfNode.getChildCount(); k++) {
                                if (pfNode.getChildAt(k) instanceof JiraPresetFilterTreeNode) {
                                    JiraPresetFilterTreeNode filterNode = (JiraPresetFilterTreeNode) pfNode.getChildAt(k);
                                    if (filterNode.getPresetFilter().getClass().getCanonicalName().equals(filterClass)) {
                                        setSelectionPath(new TreePath(filterNode.getPath()));
                                        scrollPathToVisible(new TreePath(filterNode.getPath()));
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean setSelectionManualFilter(final ServerId serverId, String filterId) {
        DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (this.getModel().getRoot()));
        if (rootNode == null) {
            return false;
        }
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            if (rootNode.getChildAt(i) instanceof JIRAServerTreeNode) {
                JIRAServerTreeNode node = (JIRAServerTreeNode) rootNode.getChildAt(i);
                if (node.getJiraServer() != null && node.getJiraServer().getServerId().equals(serverId)) {
                    for (int j = 0; j < node.getChildCount(); j++) {
                        if (node.getChildAt(j) instanceof JiraCustomFiltersGroupTreeNode) {
                            JiraCustomFiltersGroupTreeNode cfNode = (JiraCustomFiltersGroupTreeNode) node.getChildAt(j);
                            for (int k = 0; k < cfNode.getChildCount(); k++) {
                                if (cfNode.getChildAt(k) instanceof JIRAManualFilterTreeNode) {
                                    JIRAManualFilterTreeNode manualFilterNode = (JIRAManualFilterTreeNode) cfNode.getChildAt(k);
                                    if (manualFilterNode.getManualFilter().getUid().equals(filterId)) {

                                        // single manual filter support
//									if (manualFilterNode.getManualFilterSet().equals(manualFilter)) {
                                        setSelectionPath(new TreePath(manualFilterNode.getPath()));
                                        scrollPathToVisible(new TreePath(manualFilterNode.getPath()));
                                        return true;
                                    }
//							}
                                }
                            }
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

        for (JiraServerData server : aListModel.getJIRAServers()) {
            JIRAServerTreeNode serverNode = new JIRAServerTreeNode(projectCfgManager, server);
            createFilterNodes(project, server, serverNode, aListModel);
            rootNode.add(serverNode);
        }
    }

    private void createFilterNodes(Project p, JiraServerData jiraServer, DefaultMutableTreeNode node,
                                   JIRAFilterListModel aListModel) {
        if (aListModel != null) {

            JiraPresetFiltersGroupTreeNode pfg = new JiraPresetFiltersGroupTreeNode(projectCfgManager, jiraServer);
            node.add(pfg);

            Collection<JiraPresetFilter> presetFilterSet = aListModel.getPresetFilters(p, jiraServer);
            for (JiraPresetFilter filter : presetFilterSet) {
                pfg.add(new JiraPresetFilterTreeNode(filter));
            }

            JiraSavedFiltersGroupTreeNode sfg = new JiraSavedFiltersGroupTreeNode(projectCfgManager, jiraServer);
            node.add(sfg);

            for (JIRASavedFilter savedFilter : aListModel.getSavedFilters(jiraServer)) {
                sfg.add(new JIRASavedFilterTreeNode(projectCfgManager, savedFilter, jiraServer));
            }

            Set<JiraCustomFilter> manualFilterSet = aListModel.getManualFilters(jiraServer);

            JiraCustomFiltersGroupTreeNode cfg = new JiraCustomFiltersGroupTreeNode(projectCfgManager, jiraServer);
            node.add(cfg);

            for (JiraCustomFilter filter : manualFilterSet) {
                cfg.add(new JIRAManualFilterTreeNode(projectCfgManager, filter, jiraServer));
            }
        }

    }

    public void addSelectionListener(final JiraFilterTreeSelectionListener jiraFilterTreeSelectionListener) {
        selectionListeners.add(jiraFilterTreeSelectionListener);
    }

    public void removeSelectionListener(final JiraFilterTreeSelectionListener jiraFilterTreeSelectionListener) {
        selectionListeners.remove(jiraFilterTreeSelectionListener);
    }

    public void updatePresetFiltersNodes(final JiraServerData serverData) {
            final DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();

        if (serverData == null) {
            return;
        }
        
        for (int i = 0; i < root.getChildCount(); i++) {

             if (root.getChildAt(i) instanceof JIRAServerTreeNode
                     && ((JIRAServerTreeNode) root.getChildAt(i)).getJiraServer().equals(serverData)) {
                 JIRAServerTreeNode serverNode =  (JIRAServerTreeNode) root.getChildAt(i);
                    for (int j = 0; j <  serverNode.getChildCount(); j++) {
                        if (serverNode.getChildAt(j) instanceof JiraPresetFiltersGroupTreeNode) {
                            JiraPresetFiltersGroupTreeNode presetGroup
                                    = (JiraPresetFiltersGroupTreeNode) serverNode.getChildAt(j);
                            for (int k = 0; k < presetGroup.getChildCount(); k++) {
                                JiraPresetFilter filter = ((JiraPresetFilterTreeNode) presetGroup.getChildAt(k))
                                        .getPresetFilter();
                                filter.setJiraProject(jiraWorkspaceConfiguration.getPresetFilterProject(serverData, filter));
                            }
                        }
                    }
             }
        }
    }

    private class LocalTreeSelectionListener implements TreeSelectionListener {
        private JiraPresetFilter prevPresetFilter = null;
        private JiraCustomFilter prevManualFilter = null;
        private JIRASavedFilter prevSavedFilter = null;
        private ServerData prevServer = null;
        private boolean prevRecentlyOpen = false;

        public final void valueChanged(final TreeSelectionEvent event) {

            JiraPresetFilter presetFilter = getSelectedPresetFilter();
            JiraCustomFilter manualFilter = getSelectedManualFilter();
            JIRASavedFilter savedFilter = getSelectedSavedFilter();
            JiraServerData serverCfg = getSelectedServer();
            boolean recentlyOpenSelected = isRecentlyOpenSelected();

            if (isPresetFilterGroupNodeSelected()) {
                return;
            }
            if (presetFilter != null) {
                prevPresetFilter = presetFilter;
                prevManualFilter = null;
                prevSavedFilter = null;
                prevRecentlyOpen = false;
                prevServer = serverCfg;
                fireSelectedPresetFilterNode(presetFilter, serverCfg);
            } else if (manualFilter != null) {
                prevPresetFilter = null;
                prevManualFilter = manualFilter;
                prevSavedFilter = null;
                prevRecentlyOpen = false;
                prevServer = serverCfg;
                fireSelectedManualFilterNode(manualFilter, serverCfg);
            } else if (savedFilter != null) {
                prevPresetFilter = null;
                prevSavedFilter = savedFilter;
                prevManualFilter = null;
                prevRecentlyOpen = false;
                prevServer = serverCfg;
                fireSelectedSavedFilterNode(savedFilter, serverCfg);
            } else if (isPresetFilterGroupNodeSelected()) {
                int i = 0;
                //do nothing
            }  else if (isServerNodeSelected()) {
                prevPresetFilter = null;
                prevSavedFilter = null;
                prevManualFilter = null;
            } else if (recentlyOpenSelected) {
                prevPresetFilter = null;
                prevSavedFilter = null;
                prevManualFilter = null;
                prevRecentlyOpen = true;
                fireSelectedRecentlyOpenNode();
            } else {
                // all nodes unselected
                prevPresetFilter = null;
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

        private void fireSelectedSavedFilterNode(final JIRASavedFilter savedFilter, final JiraServerData jiraServerData) {
            for (JiraFilterTreeSelectionListener listener : selectionListeners) {
                listener.selectedSavedFilterNode(savedFilter, jiraServerData);
            }
        }

        private void fireSelectedPresetFilterNode(final JiraPresetFilter presetFilter, final JiraServerData jiraServerData) {
            for (JiraFilterTreeSelectionListener listener : selectionListeners) {
                listener.selectedPresetFilterNode(presetFilter, jiraServerData);
            }
        }

        private void fireSelectedManualFilterNode(final JiraCustomFilter manualFilter, final JiraServerData jiraServerData) {
            for (JiraFilterTreeSelectionListener listener : selectionListeners) {
                listener.selectedManualFilterNode(manualFilter, jiraServerData);
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

        public void manualFilterChanged(final JiraCustomFilter manualFilter, final JiraServerData jiraServer) {
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

        public void manualFilterAdded(JIRAFilterListModel jiraFilterListModel, JiraCustomFilter manualFilter,
                                      ServerId serverId) {
            rebuildTree(jiraFilterListModel, false);
            setSelectionManualFilter(serverId, manualFilter.getUid());
        }

        public void manualFilterRemoved(JIRAFilterListModel jiraFilterListModel, JiraCustomFilter manualFilter,
                                        ServerId serverId) {
            rebuildTree(jiraFilterListModel, true);
            clearSelection();

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



