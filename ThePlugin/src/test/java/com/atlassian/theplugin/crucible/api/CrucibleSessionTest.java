package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import junit.framework.TestCase;
import org.mortbay.jetty.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 14:56:43
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleSessionTest extends TestCase {

	private Server httpServer;

	protected void setUp() throws Exception {

		CxfAuthServiceMockImpl authServiceMock = new CxfAuthServiceMockImpl();

		JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
        serverFactory.setServiceClass(RpcAuthServiceName.class);
		serverFactory.setAddress(CxfAuthServiceMockImpl.VALID_URL + "/service/auth");
		serverFactory.setServiceBean(authServiceMock);
		serverFactory.create();
	}

	protected void tearDown() throws Exception {
	}

	public void testSuccessCrucibleLogin() {

        CrucibleSessionImpl crucibleSession = null;
        crucibleSession = new CrucibleSessionImpl(CxfAuthServiceMockImpl.VALID_URL);

        try {
			crucibleSession.login(CxfAuthServiceMockImpl.VALID_LOGIN, CxfAuthServiceMockImpl.VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("Login failed while expected success: " + e.getMessage());
		}
	}

	public void testFailedCrucibleLogin() {
        CrucibleSessionImpl crucibleSession = null;
        crucibleSession = new CrucibleSessionImpl(CxfAuthServiceMockImpl.VALID_URL);

        try {
			crucibleSession.login(CxfAuthServiceMockImpl.INVALID_LOGIN, CxfAuthServiceMockImpl.INVALID_PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {

		}
	}

	private void xtestCxf() throws CrucibleException {
		CrucibleSessionImpl session = null;

		session = new CrucibleSessionImpl("http://lech.atlassian.pl:8060");

		String userName = "test";
		String password = "test";

		try {
			session.login(userName, password);
		} catch (CrucibleLoginException e) {
			fail("login failed: " + e.getMessage());
		}

		session.logout();
	}
}
