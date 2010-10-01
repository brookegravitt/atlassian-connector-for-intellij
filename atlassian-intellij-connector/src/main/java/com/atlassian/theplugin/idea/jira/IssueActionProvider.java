package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.jira.JiraServerData;

public interface IssueActionProvider {

	/**
	 * Can be called from the non-UI thread
	 *
	 * @param message
	 */
	void setStatusInfoMessage(final String message);

	/**
	 * Can be called from the non-UI thread
	 *
	 * @param error
	 */
	void setStatusErrorMessage(final String error);

	/**
	 * Can be called from the non-UI thread
	 *
	 * @param error
	 * @param exception
	 */
	void setStatusErrorMessage(final String error, Throwable exception);

	JiraServerData getSelectedServer();
}
