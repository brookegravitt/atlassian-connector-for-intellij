package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.JiraCustomFilterMap;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: pmaruszak
 */
public class JIRAFilterListBuilder {
	private JIRAFilterListModel listModel;
	private JiraWorkspaceConfiguration jiraWorkspaceCfg;

	public JIRAFilterListBuilder() {
	}

	public void setListModel(@NotNull final JIRAFilterListModel listModel) {
		this.listModel = listModel;
	}

	public void setJiraWorkspaceCfg(@NotNull JiraWorkspaceConfiguration jiraWorkspaceCfg) {
		this.jiraWorkspaceCfg = jiraWorkspaceCfg;
	}

	public void rebuildModel(final JIRAServerModel jiraServerModel) throws JIRAServerFiltersBuilderException {
		if (jiraServerModel == null) {
			PluginUtil.getLogger().error("jiraServerModel must not be null");
			return;
		}
		try {
			listModel.setModelFrozen(true);
			listModel.clearAllServerFilters();
			JIRAServerFiltersBuilderException e = new JIRAServerFiltersBuilderException();
			Collection<ServerData> serversToAdd = new ArrayList<ServerData>();

			for (ServerData jiraServer : jiraServerModel.getServers()) {
				try {
					if (jiraServerModel.getServers().contains(jiraServer)) {
						loadServerSavedFilter(jiraServer, jiraServerModel);
					} else {
						serversToAdd.add(jiraServer);
					}
				} catch (JIRAException exc) {
					e.addException(jiraServer, exc);
				}
				loadManualFilter(jiraServer);
			}
			//add non existing servers
			for (ServerData newServer : serversToAdd) {
				try {
					loadServerSavedFilter(newServer, jiraServerModel);
				} catch (JIRAException e1) {
					e.addException(newServer, e1);
				}
			}

			if (!e.getExceptions().isEmpty()) {
				listModel.setModelFrozen(false);
				throw e;
			}
		} finally {
			listModel.setModelFrozen(false);
		}
	}

	private void loadServerSavedFilter(final ServerData jiraServer,
			final JIRAServerModel jiraServerModel) throws JIRAException {

		if (jiraServerModel != null) {
			List<JIRAQueryFragment> filters = jiraServerModel.getSavedFilters(jiraServer);

			List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>(filters != null ? filters.size() : 0);

			if (filters != null) {
				for (JIRAQueryFragment query : filters) {
					savedFilters.add((JIRASavedFilter) query);
				}
				listModel.setSavedFilters(jiraServer, savedFilters);
			}


		} else {
			PluginUtil.getLogger().warn("JiraServerModel is null. No saved filters retrieved.");
		}
	}

	private void loadManualFilter(final ServerData jiraServer) {

		if (jiraWorkspaceCfg != null) {

			JiraCustomFilterMap filterMap =
					jiraWorkspaceCfg.getJiraFilterConfiguaration(jiraServer.getServerId());



            for (JiraFilterConfigurationBean bean : filterMap.getCustomFilters().values()) {
            List<JIRAQueryFragment> query;

				query = getFragments(bean.getManualFilter());


			final JiraCustomFilter jiraManualFilter = new JiraCustomFilter(bean.getUid(), bean.getName(), query);
                listModel.addManualFilter(jiraServer, jiraManualFilter);
            }
        }
    }


	public boolean isModelFrozen() {
		return listModel.isModelFrozen();
	}

	public class JIRAServerFiltersBuilderException extends Exception {
		private Map<ServerData, JIRAException> exceptions = new HashMap<ServerData, JIRAException>();

		public void addException(ServerData server, JIRAException e) {
			//noinspection ThrowableResultOfMethodCallIgnored
			exceptions.put(server, e);
		}

		public Map<ServerData, JIRAException> getExceptions() {
			return exceptions;
		}
	}

	public static List<JIRAQueryFragment> getFragments(List<JiraFilterEntryBean> query) {
		List<JIRAQueryFragment> fragments = new ArrayList<JIRAQueryFragment>();

		for (JiraFilterEntryBean filterMapBean : query) {
			Map<String, String> filter = filterMapBean.getFilterEntry();
			String className = filter.get("filterTypeClass");
			try {
				Class<?> c = Class.forName(className);
				fragments.add((JIRAQueryFragment) c.getConstructor(Map.class).newInstance(filter));
			} catch (Exception e) {
				LoggerImpl.getInstance().error(e);
			}
		}
		return fragments;
	}
}
