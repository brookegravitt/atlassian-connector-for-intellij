package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public void clearManualFilter(final ServerData jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			serversFilters.get(jiraServer).getManualFilter().getQueryFragment().clear();
		}

	}

	public void setManualFilter(final ServerData jiraServer, @NotNull final JIRAManualFilter filter) {

		if (serversFilters.containsKey(jiraServer)) {
			serversFilters.get(jiraServer).setManualFilter(filter);
		} else {
			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.setManualFilter(filter);
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

	public JIRAManualFilter getManualFilter(final ServerData jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			return serversFilters.get(jiraServer).getManualFilter();
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

	public void fireManualFilterChanged(final JIRAManualFilter manualFilter, final ServerData jiraServer) {
		for (JIRAFilterListModelListener listener : listeners) {
			listener.manualFilterChanged(manualFilter, jiraServer);
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
}
