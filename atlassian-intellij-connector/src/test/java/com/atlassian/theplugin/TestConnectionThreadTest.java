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
import junit.framework.TestCase;


public class TestConnectionThreadTest extends TestCase {

	private Connector emptyConnectionTester;
	private Connector failedConnectionTester;
	private static final String ERROR_MESSAGE = "Error message";

	@Override
	public void setUp() throws Exception {

		emptyConnectionTester = new Connector() {
			public void connect(final ServerData serverCfg) throws RemoteApiException {
			}

			public void onSuccess() {
			}
		};

		failedConnectionTester = new Connector() {
			public void connect(final ServerData serverCfg) throws RemoteApiException {
				throw new RemoteApiException(ERROR_MESSAGE);
			}

			public void onSuccess() {
			}
		};
	}


	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private final ServerData serverData = new ServerData(null, null, null);

	public void testRunInterupted() {
		ConnectionWrapper testConnectionThread = new ConnectionWrapper(emptyConnectionTester, serverData,
				"test thread");

		assertEquals(ConnectionWrapper.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());
		testConnectionThread.start();
		testConnectionThread.setInterrupted();
		assertEquals(ConnectionWrapper.ConnectionState.INTERUPTED, testConnectionThread.getConnectionState());

		try {
			// wait for the connection thread
			testConnectionThread.join();
		} catch (InterruptedException e) {
			fail("TestConnecitonThread was interrupted unexpectedly");
		}

		// make sure that thread state has not been change into FAILED or SUCCEEDED
		assertEquals(ConnectionWrapper.ConnectionState.INTERUPTED, testConnectionThread.getConnectionState());
		assertNull("errorMessage should be null if there was no error", testConnectionThread.getErrorMessage());

	}

	public void testRunSucceeded() {

		ConnectionWrapper testConnectionThread = new ConnectionWrapper(emptyConnectionTester, serverData,
				"test thread");

		assertEquals(ConnectionWrapper.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());
		testConnectionThread.start();

		try {
			// wait for the connection thread
			testConnectionThread.join();
		} catch (InterruptedException e) {
			fail("TestConnecitonThread was interrupted unexpectedly");
		}

		// make sure that thread state has not changed into FAILED or SUCCEEDED
		assertEquals(ConnectionWrapper.ConnectionState.SUCCEEDED, testConnectionThread.getConnectionState());
		assertNull("errorMessage should be null if there was no error", testConnectionThread.getErrorMessage());

	}

	public void testRunFailed() {

		ConnectionWrapper testConnectionThread = new ConnectionWrapper(failedConnectionTester, serverData,
				"test thread");

		assertEquals(ConnectionWrapper.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());
		testConnectionThread.start();

		try {
			// wait for the connection thread
			testConnectionThread.join();
		} catch (InterruptedException e) {
			fail("TestConnecitonThread was interrupted unexpectedly");
		}

		// make sure that thread state has not changed into FAILED or SUCCEEDED
		assertEquals(ConnectionWrapper.ConnectionState.FAILED, testConnectionThread.getConnectionState());
		assertEquals(ERROR_MESSAGE, testConnectionThread.getErrorMessage());
	}

}
