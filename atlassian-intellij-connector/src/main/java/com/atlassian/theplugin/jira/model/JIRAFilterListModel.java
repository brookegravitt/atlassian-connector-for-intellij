package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRASavedFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModel implements FrozenModel {

	private Map<JiraServerData, JIRAServerFiltersBean> serversFilters =
            new HashMap<JiraServerData, JIRAServerFiltersBean>();
	private List<JIRAFilterListModelListener> listeners = new ArrayList<JIRAFilterListModelListener>();
	private List<FrozenModelListener> frozenModelListeners = new ArrayList<FrozenModelListener>();

	private boolean modelFrozen = false;

	public void setSavedFilters(final JiraServerData jiraServerData, @NotNull final List<JIRASavedFilter> filters) {

		if (serversFilters.containsKey(jiraServerData)) {

			serversFilters.get(jiraServerData).setSavedFilters(filters);

		} else {

			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.setSavedFilters(filters);
			serversFilters.put(jiraServerData, serverFilters);
		}
	}

	public void clearManualFilter(final JiraServerData jiraServerData, final JiraCustomFilter filter) {
		if (serversFilters.containsKey(jiraServerData)
                && serversFilters.get(jiraServerData).getManualFilters().contains(filter)) {
			for (JiraCustomFilter f : serversFilters.get(jiraServerData).getManualFilters()) {
                    if (filter.equals(f)) {
                        f.getQueryFragment().clear();
                    }
            }
		}

	}

	public void addManualFilter(final JiraServerData jiraServerData, @NotNull final JiraCustomFilter filter) {

		if (serversFilters.containsKey(jiraServerData)) {

			    serversFilters.get(jiraServerData).getManualFilters().add(filter);
		} else {
			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.getManualFilters().add(filter);
            
			serversFilters.put(jiraServerData, serverFilters);
		}
	}

	public List<JiraServerData> getJIRAServers() {
		return new ArrayList<JiraServerData>(serversFilters.keySet());
	}

	public List<JIRASavedFilter> getSavedFilters(final JiraServerData jiraServerData) {
		if (serversFilters.containsKey(jiraServerData)) {
			return serversFilters.get(jiraServerData).getSavedFilters();
		}
		return null;
	}

	public Set<JiraCustomFilter> getManualFilter(final JiraServerData jiraServerData) {
		if (serversFilters.containsKey(jiraServerData)) {
			return serversFilters.get(jiraServerData).getManualFilters();
		}

		return null;
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
        if (serversFilters.containsKey(jiraServerData)) {
            serversFilters.get(jiraServerData).getManualFilters().remove(filter);
        }




    }

    public void setManualFilters(JiraServerData jiraServerData, Set<JiraCustomFilter> manualFilters) {
        for (JiraCustomFilter filter : manualFilters) {
            addManualFilter(jiraServerData, filter);
        }
    }
}
