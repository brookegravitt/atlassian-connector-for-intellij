package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;

public interface IssueActionProvider {
	void setStatusMessage(final String message);

	void setStatusMessage(final String message, final boolean isError);

	JiraServerCfg getSelectedServer();
}
