package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModel implements FrozenModel {

	private Map<ServerData, JIRAServerFiltersBean> serversFilters = new HashMap<ServerData, JIRAServerFiltersBean>();
	private List<JIRAFilterListModelListener> listeners = new ArrayList<JIRAFilterListModelListener>();
	private List<FrozenModelListener> frozenModelListeners = new ArrayList<FrozenModelListener>();

	private boolean modelFrozen = false;

	public void setSavedFilters(final ServerData jiraServer, @NotNull final List<JIRASavedFilter> filters) {

		if (serversFilters.containsKey(jiraServer)) {

			serversFilters.get(jiraServer).setSavedFilters(filters);

		} else {

			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.setSavedFilters(filters);
			serversFilters.put(jiraServer, serverFilters);
		}
	}

	public void clearManualFilter(final ServerData jiraServer, final JiraCustomFilter filter) {
		if (serversFilters.containsKey(jiraServer)
                && serversFilters.get(jiraServer).getManualFilters().contains(filter)) {
			for (JiraCustomFilter f : serversFilters.get(jiraServer).getManualFilters()) {
                    if (filter.equals(f)) {
                        f.getQueryFragment().clear();
                    }
            }
		}

	}

	public void addManualFilter(final ServerData jiraServer, @NotNull final JiraCustomFilter filter) {

		if (serversFilters.containsKey(jiraServer)) {

			    serversFilters.get(jiraServer).getManualFilters().add(filter);
		} else {
			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.getManualFilters().add(filter);
            
			serversFilters.put(jiraServer, serverFilters);
		}
	}

	public List<ServerData> getJIRAServers() {
		return new ArrayList<ServerData>(serversFilters.keySet());
	}

	public List<JIRASavedFilter> getSavedFilters(final ServerData jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			return serversFilters.get(jiraServer).getSavedFilters();
		}
		return null;
	}

	public Set<JiraCustomFilter> getManualFilter(final ServerData jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			return serversFilters.get(jiraServer).getManualFilters();
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

	public void fireManualFilterChanged(final JiraCustomFilter manualFilter, final ServerData jiraServer) {
		for (JIRAFilterListModelListener listener : listeners) {
			listener.manualFilterChanged(manualFilter, jiraServer);
		}
	}

    public void fireManualFilterAdded(final JiraCustomFilter filter, final ServerData jiraServer) {
        	for (JIRAFilterListModelListener listener : listeners) {
			listener.manualFilterAdded(this, filter, jiraServer.getServerId());
		}
    }

     public void fireManualFilterRemoved(final JiraCustomFilter filter, final ServerData jiraServer) {
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

    public void removeManualFilter(ServerData jiraServer, JiraCustomFilter filter) {
        JiraCustomFilter filterToRemove = null;
        if (serversFilters.containsKey(jiraServer)) {
            serversFilters.get(jiraServer).getManualFilters().remove(filter);           
        }




    }

    public void setManualFilters(ServerData jServer, Set<JiraCustomFilter> manualFilters) {
        for (JiraCustomFilter filter : manualFilters) {
            addManualFilter(jServer, filter);
        }
    }
}
