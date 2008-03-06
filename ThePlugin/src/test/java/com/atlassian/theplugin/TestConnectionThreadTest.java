package com.atlassian.theplugin;

import junit.framework.TestCase;
import com.atlassian.theplugin.idea.config.serverconfig.ConnectionTester;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.TestConnectionThread;


public class TestConnectionThreadTest extends TestCase {

	private ConnectionTester emptyConnectionTester;
	private ConnectionTester failedConnectionTester;
	private static final String ERROR_MESSAGE = "Error message";

	public void setUp() throws Exception {

		emptyConnectionTester = new ConnectionTester() {
			public void testConnection(String username, String password, String server) throws ThePluginException {

			}
		};
		
		failedConnectionTester = new ConnectionTester() {
			public void testConnection(String username, String password, String server) throws ThePluginException {
				throw new ThePluginException(ERROR_MESSAGE);
			}
		};
	}


	public void tearDown() throws Exception {
        super.tearDown();
    }

	public void testRunInterupted() {

		TestConnectionThread testConnectionThread = new TestConnectionThread(emptyConnectionTester, null, null, null);

		assertEquals(TestConnectionThread.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());
		testConnectionThread.start();
		assertEquals(TestConnectionThread.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());
		testConnectionThread.setInterrupted();
		assertEquals(TestConnectionThread.ConnectionState.INTERUPTED, testConnectionThread.getConnectionState());

		try {
			// wait for the connection thread
			testConnectionThread.join();
		} catch (InterruptedException e) {
			fail("TestConnecitonThread was interrupted unexpectedly");
		}

		// make sure that thread state has not been change into FAILED or SUCCEEDED
		assertEquals(TestConnectionThread.ConnectionState.INTERUPTED, testConnectionThread.getConnectionState());
		assertNull("errorMessage should be null if there was no error", testConnectionThread.getErrorMessage());

	}

	public void testRunSucceeded() {

		TestConnectionThread testConnectionThread = new TestConnectionThread(emptyConnectionTester, null, null, null);

		assertEquals(TestConnectionThread.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());
		testConnectionThread.start();
		assertEquals(TestConnectionThread.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());

		try {
			// wait for the connection thread
			testConnectionThread.join();
		} catch (InterruptedException e) {
			fail("TestConnecitonThread was interrupted unexpectedly");
		}

		// make sure that thread state has not changed into FAILED or SUCCEEDED
		assertEquals(TestConnectionThread.ConnectionState.SUCCEEDED, testConnectionThread.getConnectionState());
		assertNull("errorMessage should be null if there was no error", testConnectionThread.getErrorMessage());

	}

	public void testRunFailed() {

		TestConnectionThread testConnectionThread = new TestConnectionThread(failedConnectionTester, null, null, null);

		assertEquals(TestConnectionThread.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());
		testConnectionThread.start();
		assertEquals(TestConnectionThread.ConnectionState.NOT_FINISHED, testConnectionThread.getConnectionState());

		try {
			// wait for the connection thread
			testConnectionThread.join();
		} catch (InterruptedException e) {
			fail("TestConnecitonThread was interrupted unexpectedly");
		}

		// make sure that thread state has not changed into FAILED or SUCCEEDED
		assertEquals(TestConnectionThread.ConnectionState.FAILED, testConnectionThread.getConnectionState());
		assertEquals(ERROR_MESSAGE, testConnectionThread.getErrorMessage());
	}

}
