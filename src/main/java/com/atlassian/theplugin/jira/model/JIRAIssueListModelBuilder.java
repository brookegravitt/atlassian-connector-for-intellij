package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;

import java.util.List;

public interface JIRAIssueListModelBuilder {
	void setModel(JIRAIssueListModel model);
	JIRAIssueListModel getModel();
	void setServer(JiraServerCfg server);
	JiraServerCfg getServer();
	void setSavedFilter(JIRASavedFilter filter);
	void setCustomFilter(List<JIRAQueryFragment> query);
	void addIssuesToModel(int size, boolean reload) throws JIRAException;
	void updateIssue(JIRAIssue issue) throws JIRAException;
	void reset();
}
