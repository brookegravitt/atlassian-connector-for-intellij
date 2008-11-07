package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraFiltersBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
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
public class JIRAServerFiltersBuilder {
	private JIRAFilterListModel listModel;

	private ProjectConfigurationBean projectConfigurationBean;

	private ProjectId projectId;


	private final JIRAServerFacade jiraServerFacade = JIRAServerFacadeImpl.getInstance();

	public JIRAServerFiltersBuilder() {
	}

	public void setProjectId(final ProjectId projectId) {
		this.projectId = projectId;
	}

	public void setListModel(@NotNull final JIRAFilterListModel listModel) {
		this.listModel = listModel;
	}

	public void setProjectConfigurationBean(@NotNull ProjectConfigurationBean projectConfigurationBean) {
		this.projectConfigurationBean = projectConfigurationBean;
	}


	public void refreshFiltersAll() throws JIRAServerFiltersBuilderException {
		JIRAServerFiltersBuilderException e = new JIRAServerFiltersBuilderException();
		for (JiraServerCfg jiraServer : IdeaHelper.getCfgManager().getAllEnabledJiraServers(projectId)) {
			try {
				refreshManualFilter(jiraServer);
				refreshServerSavedFilter(jiraServer);
			} catch (JIRAException exc) {
				e.addException(jiraServer, exc);
			}
		}
		if (!e.getExceptions().isEmpty()) {
			throw e;
		}
	}

	public void refreshServerSavedFilter(final JiraServerCfg jiraServer) throws JIRAException {
		List<JIRAQueryFragment> filters = null;

		filters = jiraServerFacade.getSavedFilters(jiraServer);
		List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>(filters.size());

		for (JIRAQueryFragment query : filters) {

			savedFilters.add((JIRASavedFilter) query);
		}

		listModel.setSavedFilters(jiraServer, savedFilters);


	}

	private void refreshManualFilter(final JiraServerCfg jiraServer) {

		if (projectConfigurationBean != null && projectConfigurationBean.getJiraConfiguration() != null) {
			JiraFiltersBean bean = projectConfigurationBean.getJiraConfiguration()
					.getJiraFilters(jiraServer.getServerId().toString());

			if (bean != null) {
				listModel.setManualFilter(jiraServer, getFragments(bean.getManualFilter()));
			}
		}
	}

	public class JIRAServerFiltersBuilderException extends Exception {
		private Map<JiraServerCfg, JIRAException> exceptions = new HashMap<JiraServerCfg, JIRAException>();

		public void addException(JiraServerCfg server, JIRAException e) {
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
