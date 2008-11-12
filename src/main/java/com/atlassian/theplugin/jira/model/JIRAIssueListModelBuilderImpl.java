package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;

import java.util.ArrayList;
import java.util.List;

public final class JIRAIssueListModelBuilderImpl implements JIRAIssueListModelBuilder {
	private JiraServerCfg server;
	private JIRASavedFilter savedFilter;
	private List<JIRAQueryFragment> customFilter;
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

	public void setServer(JiraServerCfg server) {
		this.server = server;
	}

	public JiraServerCfg getServer() {
		return server;
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

	public synchronized void addIssuesToModel(int size, boolean reload) throws JIRAException {
		if (server == null || model == null || !(customFilter != null || savedFilter != null)) {
			model.notifyListeners();
			return;
		}

		if (reload) {
			startFrom = 0;
			model.clear();
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

	public void updateIssue(final JIRAIssue issue) throws JIRAException {
		//@todo implement
		//assert(false);
	}

	public synchronized void reset() {
		int size = model.getIssues().size();
		if (size > 0) {
			model.clear();
			startFrom = 0;
			model.notifyListeners();
		}
	}
}
