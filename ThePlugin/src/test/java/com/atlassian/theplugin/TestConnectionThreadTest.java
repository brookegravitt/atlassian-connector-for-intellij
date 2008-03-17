package com.atlassian.theplugin;

import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.util.Connector;
import junit.framework.TestCase;


public class TestConnectionThreadTest extends TestCase {

	private Connector emptyConnectionTester;
	private Connector failedConnectionTester;
	private static final String ERROR_MESSAGE = "Error message";

	public void setUp() throws Exception {

		emptyConnectionTester = new Connector() {
			public void connect() throws ThePluginException {
			}
		};
		
		failedConnectionTester = new Connector() {
			public void connect() throws ThePluginException {
				throw new ThePluginException(ERROR_MESSAGE);
			}
		};
	}


	public void tearDown() throws Exception {
        super.tearDown();
    }

	public void testRunInterupted() {

		ConnectionWrapper testConnectionThread = new ConnectionWrapper(emptyConnectionTester);

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

		ConnectionWrapper testConnectionThread = new ConnectionWrapper(emptyConnectionTester);

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

		ConnectionWrapper testConnectionThread = new ConnectionWrapper(failedConnectionTester);

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
