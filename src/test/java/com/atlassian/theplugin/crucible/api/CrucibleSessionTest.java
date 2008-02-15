package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import junit.framework.TestCase;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.List;

public class CrucibleSessionTest extends TestCase {
	private RpcAuthServiceName authServiceMock;
	private CxfReviewServiceMockImpl reviewServiceMock;
	private static final String INVALID_LOGIN = "invalidLogin";
	private static final String VALID_LOGIN = "validLogin";
	private static final String INVALID_PASSWORD = "invalidPassword";
	private static final String VALID_PASSWORD = "validPassword";
	private static final String VALID_AUTH_URL = "http://localhost:9000";
	private Server reviewServer;
	private Server authServer;

	protected void setUp() throws Exception {

		authServiceMock = EasyMock.createStrictMock(RpcAuthServiceName.class);

		JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
		serverFactory.setServiceClass(RpcAuthServiceName.class);
		serverFactory.setAddress(VALID_AUTH_URL + "/service/auth");
		serverFactory.setServiceBean(authServiceMock);
		authServer = serverFactory.create();

		reviewServiceMock = new CxfReviewServiceMockImpl();

		JaxWsServerFactoryBean reviewServerFactory = new JaxWsServerFactoryBean();
		reviewServerFactory.setServiceClass(RpcReviewServiceName.class);
		reviewServerFactory.setAddress(CxfReviewServiceMockImpl.VALID_URL + "/service/review");
		reviewServerFactory.setServiceBean(reviewServiceMock);
		reviewServer = reviewServerFactory.create();

	}

	protected void tearDown() throws Exception {
		authServer.stop();
		reviewServer.stop();
	}

	public void testSuccessCrucibleLogin() {

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_AUTH_URL);

		authServiceMock.login(VALID_LOGIN, VALID_PASSWORD);
		EasyMock.expectLastCall().andReturn("test token");
		EasyMock.replay(authServiceMock);

		try {
			crucibleSession.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("Login failed while expected success: " + e.getMessage());
		}
		EasyMock.verify(authServiceMock);
	}

	public void testSuccessCrucibleLogout() throws Exception {

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_AUTH_URL);

		authServiceMock.login(VALID_LOGIN, VALID_PASSWORD);
		EasyMock.expectLastCall().andReturn("test token");
		authServiceMock.logout("test token");
		EasyMock.replay(authServiceMock);

		crucibleSession.login(VALID_LOGIN, VALID_PASSWORD);
		crucibleSession.logout();
		crucibleSession.logout();

		crucibleSession = new CrucibleSessionImpl(VALID_AUTH_URL);
		crucibleSession.logout();

		EasyMock.verify(authServiceMock);
	}

	public void testFailedCrucibleLogin() {
		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_AUTH_URL);
		authServiceMock.login(INVALID_LOGIN, INVALID_PASSWORD);
		EasyMock.expectLastCall().andThrow(new RuntimeException("authentication failed"));
		EasyMock.expectLastCall().andReturn(null);
		EasyMock.expectLastCall().andReturn("");
		EasyMock.replay(authServiceMock);

		try {
			crucibleSession.login(INVALID_LOGIN, INVALID_PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {

		}

		try {
			crucibleSession.login(INVALID_LOGIN, INVALID_PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {

		}

		try {
			crucibleSession.login(INVALID_LOGIN, INVALID_PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {

		}

		EasyMock.verify(authServiceMock);
	}

	public void testGetReviewsInStates() throws Exception {

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(CxfReviewServiceMockImpl.VALID_URL);

		List<State> states = new ArrayList<State>();
		states.add(State.REVIEW);

		List<ReviewData> reviews = crucibleSession.getReviewsInStates(states);

		for (ReviewData reviewData : reviews) {
			List<String> reviewers = crucibleSession.getReviewers(reviewData.getPermaId());
			for (String reviewer : reviewers) {
				assertNotNull(reviewer);
			}
		}

	}
}
