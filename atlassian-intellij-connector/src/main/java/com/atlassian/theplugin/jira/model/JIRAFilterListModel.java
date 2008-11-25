package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModel implements FrozenModel {
	private Map<JiraServerCfg, JIRAServerFiltersBean> serversFilters = new HashMap<JiraServerCfg, JIRAServerFiltersBean>();
	private List<JIRAFilterListModelListener> listeners = new ArrayList<JIRAFilterListModelListener>();
	private List<FrozenModelListener> frozenModelListeners = new ArrayList<FrozenModelListener>();

	private JiraServerCfg jiraSelectedServer;

	private JIRASavedFilter jiraSelectedSavedFilter;

	private JIRAManualFilter jiraSelectedManualFilter;
	private boolean modelFrozen = false;

	public JiraServerCfg getJiraSelectedServer() {
		return jiraSelectedServer;
	}

	public JIRAManualFilter getJiraSelectedManualFilter() {
		return jiraSelectedManualFilter;
	}

	public void selectSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
		if (serversFilters.containsKey(jiraServer) && getSavedFilters(jiraServer).contains(savedFilter)) {
			this.jiraSelectedServer = jiraServer;
			this.jiraSelectedSavedFilter = savedFilter;
			fireSavedFilterSelected();
		}
	}

	public void selectManualFilter(final JiraServerCfg jiraServer, final JIRAManualFilter manualFilter) {
		if (serversFilters.containsKey(jiraServer)) {
			this.jiraSelectedServer = jiraServer;
			this.jiraSelectedManualFilter = manualFilter;
			fireManualFilterSelected();
		}
	}

	public JIRASavedFilter getJiraSelectedSavedFilter() {
		return jiraSelectedSavedFilter;
	}

	public void setSavedFilters(final JiraServerCfg jiraServer, @NotNull final List<JIRASavedFilter> filters) {

		if (serversFilters.containsKey(jiraServer)) {

			serversFilters.get(jiraServer).setSavedFilters(filters);

		} else {

			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.setSavedFilters(filters);
			serversFilters.put(jiraServer, serverFilters);
		}
	}

	public void clearManualFilter(final JiraServerCfg jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			serversFilters.get(jiraServer).getManualFilter().getQueryFragment().clear();
		}
		
	}

	public void setManualFilter(final JiraServerCfg jiraServer, @NotNull final JIRAManualFilter filter) {

		if (serversFilters.containsKey(jiraServer)) {
			serversFilters.get(jiraServer).setManualFilter(filter);
		} else {
			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.setManualFilter(filter);
			serversFilters.put(jiraServer, serverFilters);
		}
	}

	public List<JiraServerCfg> getJIRAServers() {
		return new ArrayList<JiraServerCfg>(serversFilters.keySet());
	}

	public List<JIRASavedFilter> getSavedFilters(final JiraServerCfg jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			return serversFilters.get(jiraServer).getSavedFilters();
		}
		return null;
	}

	public JIRAManualFilter getManualFilter(final JiraServerCfg jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			return serversFilters.get(jiraServer).getManualFilter();
		}

		return null;
	}

	public void fireSavedFilterSelected() {
		for (JIRAFilterListModelListener listener : listeners) {
			if (jiraSelectedSavedFilter != null && jiraSelectedServer != null) {
				listener.selectedSavedFilter(jiraSelectedServer, jiraSelectedSavedFilter);
			}
		}
	}

	public void fireManualFilterSelected() {
		for (JIRAFilterListModelListener listener : listeners) {
			if (jiraSelectedManualFilter != null && jiraSelectedServer != null) {
				listener.selectedManualFilter(jiraSelectedServer, jiraSelectedManualFilter.getQueryFragment());
			}
		}
	}

	public void fireModelChanged() {

		for (JIRAFilterListModelListener listener : listeners) {

			listener.modelChanged(this);
		}
	}

	public void addModelListener(JIRAFilterListModelListener listener) {

		listeners.add(listener);


	}
	public void removeModelListener(JIRAIssueListModelListener listener) {
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
		
	}

	public void removeFrozenModelListener(FrozenModel listener) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private void fireModelFrozen() {
		for (FrozenModelListener listener : frozenModelListeners) {
			listener.modelFrozen(this, this.modelFrozen);
		}
	}
}
