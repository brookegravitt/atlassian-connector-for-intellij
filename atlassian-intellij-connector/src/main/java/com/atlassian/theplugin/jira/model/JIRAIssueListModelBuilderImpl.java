package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.AbstractCfgManager;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class JIRAIssueListModelBuilderImpl implements JIRAIssueListModelBuilder {
	private JIRAServerFacade facade;

	private static final String SORT_BY = "priority";
	private static final String SORT_ORDER = "DESC";

	private int startFrom;
	private JIRAIssueListModel model;
	private Project project;
	private ProjectCfgManager projectCfgManager;
	private RecentlyOpenIssuesCache recentlyOpenIssuesCache;


	public JIRAIssueListModelBuilderImpl(RecentlyOpenIssuesCache recentlyOpenIssuesCache) {
		this.recentlyOpenIssuesCache = recentlyOpenIssuesCache;
		this.project = null;
		this.projectCfgManager = null;
		facade = JIRAServerFacadeImpl.getInstance();
		startFrom = 0;
	}

	// for testing
	public void setFacade(JIRAServerFacade newFacade) {
		facade = newFacade;
	}

	public void setModel(final JIRAIssueListModel model) {
		this.model = model;
	}

	public JIRAIssueListModel getModel() {
		return model;
	}


	public synchronized void addIssuesToModel(final JIRAManualFilter manualFilter, final ServerData jiraServerCfg, int size,
			boolean reload) throws JIRAException {
		List<JIRAIssue> l = null;
		try {
			model.setModelFrozen(true);
			if (jiraServerCfg == null || model == null || manualFilter == null) {
				if (model != null) {
					model.clear();
					model.fireModelChanged();
				}
				return;
			}

			if (reload) {
				startFrom = 0;
				model.clear();
			}

			if (recentlyOpenIssuesCache != null) {
				recentlyOpenIssuesCache.loadRecenltyOpenIssues();
			}

			if (manualFilter.getQueryFragment().size() > 0) {
				l = facade.getIssues(jiraServerCfg, manualFilter.getQueryFragment(), SORT_BY, SORT_ORDER, startFrom, size);
				model.addIssues(l);
			}
			startFrom += l != null ? l.size() : 0;
			checkActiveIssue(l);
		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(l != null ? l.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

	public synchronized void addIssuesToModel(final JIRASavedFilter savedFilter, final ServerData jiraServerCfg, int size,
			boolean reload) throws JIRAException {
		List<JIRAIssue> l = null;
		try {
			model.setModelFrozen(true);
			if (jiraServerCfg == null || model == null || savedFilter == null) {
				if (model != null) {
					model.clear();
					model.fireModelChanged();
				}
				return;
			}

			if (reload) {
				startFrom = 0;
				model.clear();
			}

			if (recentlyOpenIssuesCache != null) {
				recentlyOpenIssuesCache.loadRecenltyOpenIssues();
			}

			List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
			query.add(savedFilter);
			l = facade.getSavedFilterIssues(jiraServerCfg, query, SORT_BY, SORT_ORDER, startFrom, size);
			model.addIssues(l);

			startFrom += l != null ? l.size() : 0;
			checkActiveIssue(l);
		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(l != null ? l.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

//	public synchronized void clearCache() {
//		model.clearCache();
//	}

	public synchronized void addRecenltyOpenIssuesToModel(/*RecentlyOpenIssuesCache _recentlyOpenIssuesCache,*/ boolean reload)
			throws JIRAException {

		if (model == null) {
			return;
		}

		LinkedList<JIRAIssue> issues = null;

		try {
			model.setModelFrozen(true);

			if (reload) {
//				model.clearCache();
				startFrom = 0;
				model.clear();
			}
			issues = recentlyOpenIssuesCache.loadRecenltyOpenIssues();

			model.addIssues(issues);
			startFrom += issues.size();
			checkActiveIssue(issues);

		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(issues != null ? issues.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

	public synchronized void updateIssue(final JIRAIssue issue, final ServerData jiraServerCfg) throws JIRAException {
		model.setModelFrozen(true);
		if (model == null || jiraServerCfg == null) {
			return;
		}

		JIRAIssue updatedIssue = facade.getIssueUpdate(jiraServerCfg, issue);
		try {
			model.setModelFrozen(true);
			model.setIssue(updatedIssue);
		} finally {
			model.setModelFrozen(false);
		}

		model.fireModelChanged();

	}

	public synchronized void reset() {
//		int size = model.getIssues().size();
//		if (size > 0) {
		model.clear();
		startFrom = 0;
		model.fireModelChanged();
//		}
	}

	public void checkActiveIssue(final List<JIRAIssue> newIssues) {
		ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(project);
		if (activeIssue != null) {
			for (JIRAIssue issue : newIssues) {
				if (issue.getKey().equals(activeIssue.getIssueKey()) && issue.getServer() != null
						&& issue.getServer().getServerId().toString().equals(activeIssue.getServerId())) {
					ActiveIssueUtils.checkIssueState(project, issue);
				}
			}
		}
	}

	public boolean isModelFrozen() {
		return model.isModelFrozen();
	}

	public void setModelFrozen(boolean frozen) {
		this.model.setModelFrozen(frozen);
	}

	public void addFrozenModelListener(FrozenModelListener listener) {
		this.model.addFrozenModelListener(listener);
	}

	public void removeFrozenModelListener(FrozenModelListener listener) {
		this.model.removeFrozenModelListener(listener);
	}

//	public JIRAIssue getJIRAIssue(final IssueRecentlyOpenBean recentlyOpen) throws JIRAException {
//
//		JIRAIssue foundIssue = model.getIssueFromCache(recentlyOpen);
//
//		if (foundIssue != null) {
//			return foundIssue;
//		} else if (project != null && projectCfgManager != null) {
//			JiraServerCfg jiraServer = CfgUtil
//					.getJiraServerCfgbyServerId(project, projectCfgManager, recentlyOpen.getServerId());
//			if (jiraServer != null) {
//				foundIssue = JIRAServerFacadeImpl.getInstance().getIssue(jiraServer, recentlyOpen.getIssueKey());
//
//				model.addIssue(foundIssue);
//				return foundIssue;
//			}
//
//		}
//
//		return null;
//	}

	public void setProject(final Project project) {
		this.project = project;
	}

	public void setProjectCfgManager(final ProjectCfgManager projectCfgManager) {
		this.projectCfgManager = projectCfgManager;
	}	
}
