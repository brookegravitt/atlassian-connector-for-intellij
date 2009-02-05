package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
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
public class JIRAFilterListBuilder {
	private JIRAFilterListModel listModel;
	private JiraWorkspaceConfiguration jiraWorkspaceCfg;
	private ProjectId projectId;
	private final JIRAServerFacade jiraServerFacade;
	private final CfgManager cfgManager;


	public JIRAFilterListBuilder(@NotNull final JIRAServerFacade jiraServerFacade, @NotNull final CfgManager cfgManager) {
		this.jiraServerFacade = jiraServerFacade;
		this.cfgManager = cfgManager;
	}

	public JIRAFilterListBuilder() {
		jiraServerFacade = JIRAServerFacadeImpl.getInstance();
		cfgManager = IdeaHelper.getCfgManager();
	}

	public void setProjectId(final ProjectId projectId) {
		this.projectId = projectId;
	}

	public void setListModel(@NotNull final JIRAFilterListModel listModel) {
		this.listModel = listModel;
	}

	public void setJiraWorkspaceCfg(@NotNull JiraWorkspaceConfiguration jiraWorkspaceCfg) {
		this.jiraWorkspaceCfg = jiraWorkspaceCfg;
	}

	public void rebuildModel() throws JIRAServerFiltersBuilderException {
		try {
			listModel.setModelFrozen(true);
			listModel.clearAllServerFilters();
			JIRAServerFiltersBuilderException e = new JIRAServerFiltersBuilderException();
			for (JiraServerCfg jiraServer : cfgManager.getAllEnabledJiraServers(projectId)) {
				try {
					addServerSavedFilter(jiraServer);
				} catch (JIRAException exc) {
					e.addException(jiraServer, exc);
				}
				addManualFilter(jiraServer);
			}

			if (!e.getExceptions().isEmpty()) {
				listModel.setModelFrozen(false);
				throw e;
			}
		} finally {
			listModel.setModelFrozen(false);
		}
	}

	private void addServerSavedFilter(final JiraServerCfg jiraServer) throws JIRAException {

		List<JIRAQueryFragment> filters = jiraServerFacade.getSavedFilters(jiraServer);
		List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>(filters.size());

		for (JIRAQueryFragment query : filters) {
			savedFilters.add((JIRASavedFilter) query);
		}

		listModel.setSavedFilters(jiraServer, savedFilters);
	}

	private void addManualFilter(final JiraServerCfg jiraServer) {

		if (jiraWorkspaceCfg != null) {

			List<JiraFilterEntryBean> filter =
					jiraWorkspaceCfg.getJiraFilterConfiguaration(jiraServer.getServerId().toString())
							.getManualFilterForName(JiraFilterConfigurationBean.MANUAL_FILTER_LABEL);

			List<JIRAQueryFragment> query;
			if (filter != null) {
				query = getFragments(filter);
			} else {
				//nothing found in configuration == create empty, clear filter
				query = new ArrayList<JIRAQueryFragment>();
			}

			final JIRAManualFilter jiraManualFilter = new JIRAManualFilter("Custom Filter", query);
			listModel.setManualFilter(jiraServer, jiraManualFilter);
		}
	}

	public boolean isModelFrozen() {
		return listModel.isModelFrozen();
	}

	public class JIRAServerFiltersBuilderException extends Exception {
		private Map<JiraServerCfg, JIRAException> exceptions = new HashMap<JiraServerCfg, JIRAException>();

		public void addException(JiraServerCfg server, JIRAException e) {
			//noinspection ThrowableResultOfMethodCallIgnored
			exceptions.put(server, e);
		}

		public Map<JiraServerCfg, JIRAException> getExceptions() {
			return exceptions;
		}
	}

	private static List<JIRAQueryFragment> getFragments(List<JiraFilterEntryBean> query) {
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
