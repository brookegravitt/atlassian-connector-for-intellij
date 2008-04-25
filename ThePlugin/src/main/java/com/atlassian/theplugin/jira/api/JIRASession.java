package com.atlassian.theplugin.jira.api;

import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.configuration.Server;

public interface JIRASession {
	void login(String userName, String password) throws RemoteApiLoginException;

	void logout();

	void logWork(JIRAIssue issue) throws RemoteApiException;

	boolean isLoggedIn();
}
