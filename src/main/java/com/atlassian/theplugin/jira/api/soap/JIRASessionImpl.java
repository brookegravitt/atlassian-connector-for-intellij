/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
