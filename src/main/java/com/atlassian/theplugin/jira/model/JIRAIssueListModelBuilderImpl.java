package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class JIRAIssueListModelBuilderImpl implements JIRAIssueListModelBuilder {
	private JIRAServerFacade facade;

	private static final String SORT_BY = "priority";
	private static final String SORT_ORDER = "DESC";

	private int startFrom;
	private JIRAIssueListModel model;

	public JIRAIssueListModelBuilderImpl() {
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


	public synchronized void addIssuesToModel(final JIRAManualFilter manualFilter, final JiraServerCfg jiraServerCfg, int size,
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

			if (manualFilter.getQueryFragment().size() > 0) {
				l = facade.getIssues(jiraServerCfg, manualFilter.getQueryFragment(), SORT_BY, SORT_ORDER, startFrom, size);
				model.addIssues(l);
			}
			startFrom += l != null ? l.size() : 0;
		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(l != null ? l.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

	public synchronized void addIssuesToModel(final JIRASavedFilter savedFilter, final JiraServerCfg jiraServerCfg, int size,
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

			List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
			query.add(savedFilter);
			l = facade.getSavedFilterIssues(jiraServerCfg, query, SORT_BY, SORT_ORDER, startFrom, size);
			model.addIssues(l);

			startFrom += l != null ? l.size() : 0;
		} finally {
			if (model != null) {
				model.fireModelChanged();
				model.fireIssuesLoaded(l != null ? l.size() : 0);
				model.setModelFrozen(false);
			}
		}
	}

	public synchronized void addIssuesToModel(LinkedList<IssueRecentlyOpenBean> recentlyOpenIssues,
			final Collection<JiraServerCfg> allEnabledJiraServers, int size, boolean reload) throws JIRAException {

		JIRAException exception = null;
		List<JIRAIssue> l = new ArrayList<JIRAIssue>();

		if (model == null || recentlyOpenIssues == null || recentlyOpenIssues.isEmpty()) {
			if (model != null) {
				model.clear();
				model.fireModelChanged();
			}
			return;
		}

		model.setModelFrozen(true);

		if (reload) {
			startFrom = 0;
			model.clear();
		}

		for (IssueRecentlyOpenBean recentIssue : recentlyOpenIssues) {
			for (JiraServerCfg server : allEnabledJiraServers) {
				if (server.getServerId().toString().equals(recentIssue.getServerId())) {
					JIRAIssue issue = null;
					try {
						issue = facade.getIssue(server, recentIssue.getIssueKey());
						l.add(issue);
					} catch (JIRAException e) {
						exception = e;
					}
					break;
				}
			}
		}

		model.addIssues(l);

		startFrom += l.size();

		if (model != null) {
			model.fireModelChanged();
			model.fireIssuesLoaded(l.size());
			model.setModelFrozen(false);
		}

		if (exception != null) {
			throw exception;
		}
	}

	public synchronized void updateIssue(final JIRAIssue issue, final JiraServerCfg jiraServerCfg) throws JIRAException {
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
}
