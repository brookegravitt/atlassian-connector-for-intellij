package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.crucible.api.*;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:54:14
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleServerFacadeTest extends TestCase {

	private CrucibleServerFacade facade;
	private CrucibleSession crucibleSessionMock;

	protected void setUp() {

		crucibleSessionMock = createMock(CrucibleSession.class);

		facade = CrucibleServerFactory.getCrucibleServerFacade();
		facade.setCrucibleSession(crucibleSessionMock);

	}

	public void testConnectionTestFailed() {

		try {
			crucibleSessionMock.login("badUserName", "badPassword");
			EasyMock.expectLastCall().andThrow(new CrucibleLoginException(""));
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		replay(crucibleSessionMock);

		try {
			facade.testServerConnection("some adress", "badUserName", "badPassword");
			fail("testServerConnection failed");
		} catch (CrucibleException e) {
			// testServerConnection succeed
		} finally {
			EasyMock.verify(crucibleSessionMock);
		}
	}

	public void testConnectionTestSucceed() {

		try {
			crucibleSessionMock.login("CorrectUserName", "CorrectPassword");
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		try {
			crucibleSessionMock.logout();
		} catch (CrucibleLogoutException e) {
			fail("recording mock failed for logout");
		}

		replay(crucibleSessionMock);

		try {
			facade.testServerConnection("some adress", "CorrectUserName", "CorrectPassword");
		} catch (CrucibleException e) {
			fail("testServerConnection failed");
		} finally {
			EasyMock.verify(crucibleSessionMock);
		}
	}
}
