package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

public interface IssueActionProvider {
	void setStatusMessage(final String message);

	void setStatusMessage(final String message, final boolean isError);

	ServerData getSelectedServer();
}
