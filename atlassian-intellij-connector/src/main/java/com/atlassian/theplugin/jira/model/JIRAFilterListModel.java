package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.model.presetfilters.AddedRecentlyPresetFilter;
import com.atlassian.theplugin.jira.model.presetfilters.AssignedToMePresetFilter;
import com.atlassian.theplugin.jira.model.presetfilters.MostImportantPresetFilter;
import com.atlassian.theplugin.jira.model.presetfilters.OutstandingPresetFilter;
import com.atlassian.theplugin.jira.model.presetfilters.ReportedByMePresetFilter;
import com.atlassian.theplugin.jira.model.presetfilters.ResolvedRecentlyPresetFilter;
import com.atlassian.theplugin.jira.model.presetfilters.UnscheduledPresetFilter;
import com.atlassian.theplugin.jira.model.presetfilters.UpdatedRecentlyPresetFilter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModel implements FrozenModel {
    private final ProjectCfgManager projectCfgManager;

    public JIRAFilterListModel(ProjectCfgManager projectCfgManager) {
        this.projectCfgManager = projectCfgManager;
    }

    private Map<ServerId, JIRAServerFiltersBean> serversFilters =
            new HashMap<ServerId, JIRAServerFiltersBean>();
    private List<JIRAFilterListModelListener> listeners = new ArrayList<JIRAFilterListModelListener>();
    private List<FrozenModelListener> frozenModelListeners = new ArrayList<FrozenModelListener>();

    private boolean modelFrozen = false;

    public void setSavedFilters(final JiraServerData jiraServerData, @NotNull final List<JIRASavedFilter> filters) {

        if (serversFilters.containsKey(jiraServerData.getServerId())) {

            serversFilters.get(jiraServerData.getServerId()).setSavedFilters(filters);

        } else {

            JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
            serverFilters.setSavedFilters(filters);
            serversFilters.put(jiraServerData.getServerId(), serverFilters);
        }
    }

    public void clearManualFilter(final JiraServerData jiraServerData, final JiraCustomFilter filter) {
        if (serversFilters.containsKey(jiraServerData.getServerId())
                && serversFilters.get(jiraServerData.getServerId()).getManualFilters().contains(filter)) {
            for (JiraCustomFilter f : serversFilters.get(jiraServerData.getServerId()).getManualFilters()) {
                if (filter.equals(f)) {
                    f.getQueryFragment().clear();
                }
            }
        }

    }

    public void addManualFilter(final JiraServerData jiraServerData, @NotNull final JiraCustomFilter filter) {

        if (serversFilters.containsKey(jiraServerData.getServerId())) {

            serversFilters.get(jiraServerData.getServerId()).getManualFilters().add(filter);
        } else {
            JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
            serverFilters.getManualFilters().add(filter);

            serversFilters.put(jiraServerData.getServerId(), serverFilters);
        }
    }

    public List<JiraServerData> getJIRAServers() {
        List<JiraServerData> servers = new ArrayList<JiraServerData>();
        if (projectCfgManager != null) {
            for (ServerId id : serversFilters.keySet()) {
                servers.add(projectCfgManager.getJiraServerr(id));
            }
        }
        return servers;
    }

    public List<JIRASavedFilter> getSavedFilters(final JiraServerData jiraServerData) {
        if (serversFilters.containsKey(jiraServerData.getServerId())) {
            return serversFilters.get(jiraServerData.getServerId()).getSavedFilters();
        }
        return null;
    }

    public Set<JiraCustomFilter> getManualFilters(final JiraServerData jiraServerData) {
        if (serversFilters.containsKey(jiraServerData.getServerId())) {
            return serversFilters.get(jiraServerData.getServerId()).getManualFilters();
        }

        return null;
    }

    public Collection<JiraPresetFilter> getPresetFilters(Project project, JiraServerData jiraServer) {
        List<JiraPresetFilter> list = new ArrayList<JiraPresetFilter>();

        final JiraWorkspaceConfiguration workspace = IdeaHelper.getJiraWorkspaceConfiguration(project);
//        list.add(new AllPresetFilter(projectCfgManager, jiraServer));
        list.add(new OutstandingPresetFilter(projectCfgManager, jiraServer));
        list.add(new UnscheduledPresetFilter(projectCfgManager, jiraServer));
        list.add(new AssignedToMePresetFilter(projectCfgManager, jiraServer));
        list.add(new ReportedByMePresetFilter(projectCfgManager, jiraServer));
        list.add(new ResolvedRecentlyPresetFilter(projectCfgManager, jiraServer));
        list.add(new AddedRecentlyPresetFilter(projectCfgManager, jiraServer));
        list.add(new UpdatedRecentlyPresetFilter(projectCfgManager, jiraServer));
        list.add(new MostImportantPresetFilter(projectCfgManager, jiraServer));

        //set stored by user assigned project for each preset filter
        for (JiraPresetFilter filter : list) {
            filter.setJiraProject(workspace.getPresetFilterProject(jiraServer, filter));
        }
        return list;
    }

    public void fireModelChanged() {
        for (JIRAFilterListModelListener listener : listeners) {
            listener.modelChanged(this);
        }
    }

    public void fireServerRemoved() {
        for (JIRAFilterListModelListener listener : listeners) {
            listener.serverRemoved(this);
        }
    }

    public void fireServerAdded() {
        for (JIRAFilterListModelListener listener : listeners) {
            listener.serverAdded(this);
        }
    }

    public void fireServerNameChanged() {
        for (JIRAFilterListModelListener listener : listeners) {
            listener.serverNameChanged(this);
        }
    }

    public void fireManualFilterChanged(final JiraCustomFilter manualFilter, final JiraServerData jiraServerData) {
        for (JIRAFilterListModelListener listener : listeners) {
            listener.manualFilterChanged(manualFilter, jiraServerData);
        }
    }

    public void fireManualFilterAdded(final JiraCustomFilter filter, final JiraServerData jiraServerData) {
        for (JIRAFilterListModelListener listener : listeners) {
            listener.manualFilterAdded(this, filter, jiraServerData.getServerId());
        }
    }

    public void fireManualFilterRemoved(final JiraCustomFilter filter, final JiraServerData jiraServer) {
        for (JIRAFilterListModelListener listener : listeners) {
            listener.manualFilterRemoved(this, filter, jiraServer.getServerId());
        }
    }

    public void addModelListener(JIRAFilterListModelListener listener) {
        listeners.add(listener);
    }

    public void removeModelListener(JIRAFilterListModelListener listener) {
        listeners.remove(listener);
    }

    public void clearAllServerFilters() {
        serversFilters.clear();
    }

    public boolean isModelFrozen() {
        return this.modelFrozen;
    }

    public void setModelFrozen(boolean frozen) {
        this.modelFrozen = frozen;

        fireModelFrozen();
    }

    public void addFrozenModelListener(FrozenModelListener listener) {
        frozenModelListeners.add(listener);
    }

    public void removeFrozenModelListener(FrozenModelListener listener) {
        frozenModelListeners.remove(listener);
    }

    private void fireModelFrozen() {
        for (FrozenModelListener listener : frozenModelListeners) {
            listener.modelFrozen(this, this.modelFrozen);
        }
    }

    public void removeManualFilter(JiraServerData jiraServerData, JiraCustomFilter filter) {
        JiraCustomFilter filterToRemove = null;
        if (serversFilters.containsKey(jiraServerData.getServerId())) {
            serversFilters.get(jiraServerData.getServerId()).getManualFilters().remove(filter);
        }
    }

    public void setManualFilters(JiraServerData jiraServerData, Set<JiraCustomFilter> manualFilters) {
        for (JiraCustomFilter filter : manualFilters) {
            addManualFilter(jiraServerData, filter);
        }
    }
}
