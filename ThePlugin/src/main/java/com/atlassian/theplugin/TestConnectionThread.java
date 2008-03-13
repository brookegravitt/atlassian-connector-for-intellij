package com.atlassian.theplugin;

import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.config.serverconfig.ConnectionTester;


public class TestConnectionThread extends Thread {
	private String url;
	private String userName;
	private String password;

	private ConnectionTester connectionTester;
	private String errorMessage = null;

	public String getErrorMessage() {
		return errorMessage;
	}

	public enum ConnectionState {
		SUCCEEDED,
		FAILED,
		INTERUPTED,
		NOT_FINISHED
	}

	private ConnectionState connectionState = ConnectionState.NOT_FINISHED;

	public TestConnectionThread(ConnectionTester tester, String url, String userName, String password) {
		this.connectionTester = tester;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}

	/**
	 * Runs test connection method on a ConnectionTester and sets connestionStates accordingly.
	 * That method should not be used directly but using 'start' method on a thread object. 
	 */
	public void run() {

		try {
			connectionTester.testConnection(userName, password, url);
			if (connectionState != ConnectionState.INTERUPTED) {
				connectionState = ConnectionState.SUCCEEDED;
			}
		} catch (ThePluginException e) {
			if (connectionState != ConnectionState.INTERUPTED) {
				connectionState = ConnectionState.FAILED;
				errorMessage = e.getMessage();
			}
		}

		// at this point we should have connection in state INTERUPTED, SUCCEEDED or FAILED
	}

	/**
	 * Sends request to thread to stop
	 */
	public void setInterrupted() {
		connectionState = ConnectionState.INTERUPTED;
	}

	/**
	 * @return state of current connection
	 */
	public ConnectionState getConnectionState() {
		return connectionState;
	}
}
