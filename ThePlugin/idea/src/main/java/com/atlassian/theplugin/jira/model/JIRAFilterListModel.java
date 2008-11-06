package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModel {
	private Map<JiraServerCfg, JIRAServerFiltersBean> serversFilters = new HashMap<JiraServerCfg, JIRAServerFiltersBean>();
	private List<JIRAFilterListModelListener> listeners = new ArrayList<JIRAFilterListModelListener>();

	public void setSavedFilters(final JiraServerCfg jiraServer, @NotNull final List<JIRASavedFilter> filters) {

		if (serversFilters.containsKey(jiraServer)) {

			serversFilters.get(jiraServer).setSavedFilters(filters);

		} else {

			JIRAServerFiltersBean serverFilters = new JIRAServerFiltersBean();
			serverFilters.setSavedFilters(filters);
			serversFilters.put(jiraServer, serverFilters);
		}
	}

	public void setManualFilter(final JiraServerCfg jiraServer, @NotNull final List<JIRAQueryFragment> filter) {

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

	public List<JIRASavedFilter> getSavedFilters(final JiraServerCfg jiraServer){
		if (serversFilters.containsKey(jiraServer)) {
			return serversFilters.get(jiraServer).getSavedFilters();
		}
		return null;
	}

	public List<JIRAQueryFragment> getManualFilter(final JiraServerCfg jiraServer) {
		if (serversFilters.containsKey(jiraServer)) {
			return serversFilters.get(jiraServer).getManualFilter();
		}

		return null;
	}


	public void notifyListeners() {

		for (JIRAFilterListModelListener listener : listeners) {

			listener.modelChanged(this);
	}}

	public void addModelListener(JIRAFilterListModelListener listener) {

		listeners.add(listener);


	}
	public void removeModelListener(JIRAIssueListModelListener listener) {
		listeners.remove(listener);
	}

}
