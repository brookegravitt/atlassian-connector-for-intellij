package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.JIRAServerFacade;
import com.atlassian.theplugin.commons.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class JIRAIssueListModelBuilderImpl implements JIRAIssueListModelBuilder {
	private JIRAServerFacade facade;

	private static final String SORT_BY = "priority";
	private static final String SORT_ORDER = "DESC";

	private int startFrom;
	private JIRAIssueListModel model;
	private Project project;
	private RecentlyOpenIssuesCache recentlyOpenIssuesCache;


	public JIRAIssueListModelBuilderImpl(RecentlyOpenIssuesCache recentlyOpenIssuesCache) {
		this.recentlyOpenIssuesCache = recentlyOpenIssuesCache;
		this.project = null;
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


	public synchronized void addIssuesToModel(final JiraCustomFilter manualFilter,
                                              final JiraServerData jiraServerCfg, int size,
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
				startFrom += l != null ? l.size() : 0;
//				checkActiveIssue(l);
			}
		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(l != null ? l.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

	public synchronized void addIssuesToModel(final JIRASavedFilter savedFilter,
                                              final JiraServerData jiraServerCfg, int size,
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
//			checkActiveIssue(l);
		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(l != null ? l.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

	public synchronized void addRecenltyOpenIssuesToModel(boolean reload) {

		if (model == null) {
			return;
		}

		List<JIRAIssue> issues = null;

		try {
			model.setModelFrozen(true);

			if (reload) {
				startFrom = 0;
				model.clear();
			}
			issues = recentlyOpenIssuesCache.loadRecenltyOpenIssues();

			model.addIssues(issues);
			startFrom += issues.size();
//			checkActiveIssue(issues);

		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(issues != null ? issues.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

	/**
	 * Retrieves issue from the server and updates model
	 *
	 * @param issueKey	  issue to reload
	 * @param jiraServerCfg server
	 * @throws JIRAException
	 */
	public synchronized void reloadIssue(final String issueKey,
                                         final JiraServerData jiraServerCfg) throws JIRAException {
		if (model == null || jiraServerCfg == null) {
			return;
		}
		model.setModelFrozen(true);

		JIRAIssue updatedIssue;

		try {
			updatedIssue = facade.getIssue(jiraServerCfg, issueKey);

			if (recentlyOpenIssuesCache != null) {
				recentlyOpenIssuesCache.updateIssue(updatedIssue);
			}

			model.updateIssue(updatedIssue);
		} finally {
			model.setModelFrozen(false);
		}
		model.fireIssueUpdated(updatedIssue);
		model.fireModelChanged();
	}

	/**
	 * Updates the given issue in the model. That method does not retrieve issue from the server.
	 * Can be called from the UI thread
	 *
	 * @param issue fresh issue to update in the model
	 */
	public synchronized void updateIssue(final JIRAIssue issue) {
		if (model == null) {
			return;
		}

		if (recentlyOpenIssuesCache != null) {
			recentlyOpenIssuesCache.updateIssue(issue);
		}

		model.setModelFrozen(true);
		model.updateIssue(issue);
		model.setModelFrozen(false);
		model.fireIssueUpdated(issue);
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

	public void checkActiveIssue(final Collection<JIRAIssue> newIssues) {
		ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(project);
		if (activeIssue != null) {
			for (JIRAIssue issue : newIssues) {
				if (issue.getKey().equals(activeIssue.getIssueKey()) && issue.getServer() != null
						&& issue.getServer().getServerId().equals(activeIssue.getServerId())) {
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

	public void setProject(final Project project) {
		this.project = project;
	}
}
