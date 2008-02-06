package com.atlassian.theplugin.crucible.api;

import junit.framework.TestCase;
import org.mortbay.jetty.Server;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 14:56:43
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleSessionTest extends TestCase {

	private String mockUrl;
	private Server httpServer;

	protected void setUp() throws Exception {
		super.setUp();

		httpServer = new Server(0);
		httpServer.start();

		mockUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
	}

	protected void tearDown() throws Exception {
		mockUrl = null;
		httpServer.stop();
	}

	public void testSuccessCrucibleLogin() {

		CrucibleSession session = new CrucibleSession(mockUrl);

		String userName = "username";
		String password = "password";

//		try {
//			session.login(userName, password);
//		} catch (CrucibleLoginException e) {
//			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		}

		// create and use mock for login here

		//fail("Not yet implemented");
	}

	public void testFailedCrucibleLogin() {
		//fail("Not yet implemented");
	}
}
