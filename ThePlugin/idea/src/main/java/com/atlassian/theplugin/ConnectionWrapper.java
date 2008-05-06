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

package com.atlassian.theplugin;

import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.util.Connector;


public class ConnectionWrapper extends Thread {

	private String errorMessage = null;
	private Connector connector;

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

	public ConnectionWrapper(Connector connector, String threadName) {
		super(threadName);
		this.connector = connector;
	}

	/**
	 * Runs test connection method on a ConnectionTester and sets connestionStates accordingly.
	 * That method should not be used directly but using 'start' method on a thread object. 
	 */
	public void run() {
		try {
			connector.connect();
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
