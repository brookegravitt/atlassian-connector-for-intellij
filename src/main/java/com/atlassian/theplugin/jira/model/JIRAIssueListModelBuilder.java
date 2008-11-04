package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;

import java.util.List;

public interface JIRAIssueListModelBuilder {
	void setServer(JiraServerCfg server);
	void setSavedFilter(JIRASavedFilter filter);
	void setCustomFilter(List<JIRAQueryFragment> query);
	void addIssuesToModel(JIRAIssueListModel model, int size)  throws JIRAException;
	void reset(JIRAIssueListModel model);
}
