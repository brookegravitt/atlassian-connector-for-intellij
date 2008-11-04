package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;

import java.util.List;
import java.util.ArrayList;

public final class JIRAIssueListModelBuilderImpl implements JIRAIssueListModelBuilder {
	private JiraServerCfg server;
	private JIRASavedFilter savedFilter;
	private List<JIRAQueryFragment> customFilter;
	private JIRAServerFacade facade;

	private static final String SORT_BY = "priority";
	private static final String SORT_ORDER = "DESC";

	private int startFrom;

	private JIRAIssueListModelBuilderImpl() {
		facade = JIRAServerFacadeImpl.getInstance();
		startFrom = 0;
	}

	public static JIRAIssueListModelBuilder createInstance() {
		return new JIRAIssueListModelBuilderImpl();
	}

	// for testing
	public void setFacade(JIRAServerFacade newFacade) {
		facade = newFacade;
	}

	public void setServer(JiraServerCfg server) {
		this.server = server;
	}

	public void setSavedFilter(JIRASavedFilter filter) {
		savedFilter = filter;
		customFilter = null;
		startFrom = 0;
	}

	public void setCustomFilter(List<JIRAQueryFragment> query) {
		customFilter = query;
		savedFilter = null;
		startFrom = 0;
	}

	public void addIssuesToModel(JIRAIssueListModel model, int size) throws JIRAException {
		if (server == null || !(customFilter != null || savedFilter != null)) {
			return;
		}

		List<JIRAIssue> l = null;
		if (customFilter != null) {
			l = facade.getIssues(server, customFilter, SORT_BY, SORT_ORDER, startFrom, size);
			model.addIssues(l);
		}
		if (savedFilter != null) {
			List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
			query.add(savedFilter);
			l = facade.getSavedFilterIssues(server, query, SORT_BY, SORT_ORDER, startFrom, size);
			model.addIssues(l);
		}
		startFrom += l.size();
		model.notifyListeners();
	}

	public void reset(JIRAIssueListModel model) {
		int size = model.getIssues().size();
		if (size > 0) {
			model.clear();
			startFrom = 0;
			model.notifyListeners();
		}
	}
}
