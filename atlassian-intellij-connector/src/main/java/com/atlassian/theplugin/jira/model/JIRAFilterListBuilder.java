package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.JiraCustomFilterMap;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			Collection<JiraServerData> connectionsToAdd = new ArrayList<JiraServerData>();

			for (JiraServerData jiraServerData : jiraServerModel.getServers()) {
				try {
					loadServerSavedFilter(jiraServerData, jiraServerModel);
				} catch (JIRAException exc) {
					e.addException(jiraServerData, exc);
				}
				loadManualFilter(jiraServerData);
			}
			//add non existing servers
			for (JiraServerData newServer : connectionsToAdd) {
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

	private void loadServerSavedFilter(final JiraServerData jiraServerData,
			final JIRAServerModel jiraServerModel) throws JIRAException {

		if (jiraServerModel != null) {
			List<JIRAQueryFragment> filters = jiraServerModel.getSavedFilters(jiraServerData);

			List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>(filters != null ? filters.size() : 0);

			if (filters != null) {
				for (JIRAQueryFragment query : filters) {
					savedFilters.add((JIRASavedFilter) query);
				}
				listModel.setSavedFilters(jiraServerData, savedFilters);
			}


		} else {
			PluginUtil.getLogger().warn("JiraServerModel is null. No saved filters retrieved.");
		}
	}

	private void loadManualFilter(final JiraServerData jiraServerData) {

		if (jiraWorkspaceCfg != null) {

			JiraCustomFilterMap filterMap =
					jiraWorkspaceCfg.getJiraFilterConfiguaration(jiraServerData.getServerId());



            for (JiraFilterConfigurationBean bean : filterMap.getCustomFilters().values()) {
            List<JIRAQueryFragment> query;

				query = getFragments(bean.getManualFilter());


			final JiraCustomFilter jiraManualFilter = new JiraCustomFilter(bean.getUid(), bean.getName(), query);
                listModel.addManualFilter(jiraServerData, jiraManualFilter);
            }
        }
    }

    public Collection<JiraPresetFilter> getPresetFilters(Project project, JiraServerData serverData) {
        return listModel.getPresetFilters(project, serverData);
    }

	public boolean isModelFrozen() {
		return listModel.isModelFrozen();
	}

	public class JIRAServerFiltersBuilderException extends Exception {
		private Map<JiraServerData, JIRAException> exceptions = new HashMap<JiraServerData, JIRAException>();

		public void addException(JiraServerData jiraServerData, JIRAException e) {
			//noinspection ThrowableResultOfMethodCallIgnored
			exceptions.put(jiraServerData, e);
		}

		public Map<JiraServerData, JIRAException> getExceptions() {
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
