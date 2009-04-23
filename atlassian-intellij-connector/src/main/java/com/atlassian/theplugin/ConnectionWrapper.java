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

import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.util.Connector;


public class ConnectionWrapper extends Thread {

	private Connector connector;
	private final ServerData serverCfg;
	private Exception exception;

	public Exception getException() {
		return exception;
	}

	public String getErrorMessage() {
		if (exception == null) {
			return null;
		}
		return exception.getMessage();
	}

	public enum ConnectionState {
		SUCCEEDED,
		FAILED,
		INTERUPTED,
		NOT_FINISHED
	}

	private ConnectionState connectionState = ConnectionState.NOT_FINISHED;

	public ConnectionWrapper(Connector connector, ServerData serverData, String threadName) {
		super(threadName);
		this.connector = connector;
		this.serverCfg = serverData;
	}

	/**
	 * Runs test connection method on a ConnectionTester and sets connestionStates accordingly.
	 * That method should not be used directly but using 'start' method on a thread object.
	 */
	@Override
	public void run() {
		try {
			connector.connect(serverCfg);
			if (connectionState != ConnectionState.INTERUPTED) {
				connectionState = ConnectionState.SUCCEEDED;
			}
		} catch (RemoteApiException e) {
			if (connectionState != ConnectionState.INTERUPTED) {
				connectionState = ConnectionState.FAILED;
				exception = e;
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
