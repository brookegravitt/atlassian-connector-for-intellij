package com.atlassian.theplugin.jira.api.soap;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRASession;
import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginException;

import javax.xml.rpc.ServiceException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;

public class JIRASessionImpl implements JIRASession {

	URL address;
	String token;
	JiraSoapService service;

	private boolean loggedIn = false;

	public JIRASessionImpl(URL portAddress) throws ServiceException {
		address = portAddress;
		JiraSoapServiceServiceLocator loc = new JiraSoapServiceServiceLocator();
		service = loc.getJirasoapserviceV2(address);
	}

	public void login(String userName, String password) throws RemoteApiLoginException {
		try {
			token = service.login(userName, password);
		} catch (java.rmi.RemoteException e) {
			throw new RemoteApiLoginException(e.getMessage(), e);
		}
		loggedIn = true;
	}

	public void logout() {
		try {
			if (service.logout(token)) {
				token = null;
				loggedIn = false;
			}
		} catch (java.rmi.RemoteException e) {
			// todo: log the exception
		}
	}

	public void logWork(JIRAIssue issue) throws RemoteApiException {
		RemoteWorklog workLog = new RemoteWorklog();
		Calendar yesterday = Calendar.getInstance();
		yesterday.roll(Calendar.HOUR, -24);
		workLog.setStartDate(yesterday);
		workLog.setTimeSpent("1d");
		try {
			service.addWorklogAndAutoAdjustRemainingEstimate(token, issue.getKey(), workLog);
		} catch (RemoteException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}
}
