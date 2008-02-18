package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.*;
import junit.framework.TestCase;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrucibleSessionTest extends TestCase {
	private RpcAuthServiceName authServiceMock;
	private RpcReviewServiceName reviewServiceMock;
	private static final String INVALID_LOGIN = "invalidLogin";
	private static final String VALID_LOGIN = "validLogin";
	private static final String INVALID_PASSWORD = "invalidPassword";
	private static final String VALID_PASSWORD = "validPassword";
	private static final String VALID_URL = "http://localhost:9000";
	private Server reviewServer;
	private Server authServer;

	protected void setUp() throws Exception {

		authServiceMock = EasyMock.createStrictMock(RpcAuthServiceName.class);

		JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
		serverFactory.setServiceClass(RpcAuthServiceName.class);
		serverFactory.setAddress(VALID_URL + "/service/auth");
		serverFactory.setServiceBean(authServiceMock);
		authServer = serverFactory.create();

		reviewServiceMock = EasyMock.createStrictMock(RpcReviewServiceName.class);

		JaxWsServerFactoryBean reviewServerFactory = new JaxWsServerFactoryBean();
		reviewServerFactory.setServiceClass(RpcReviewServiceName.class);
		reviewServerFactory.setAddress(VALID_URL + "/service/review");
		reviewServerFactory.setServiceBean(reviewServiceMock);
		reviewServer = reviewServerFactory.create();

	}

	protected void tearDown() throws Exception {
		authServer.stop();
		reviewServer.stop();
	}

	public void testSuccessCrucibleLogin() {

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_URL);

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

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_URL);

		authServiceMock.login(VALID_LOGIN, VALID_PASSWORD);
		EasyMock.expectLastCall().andReturn("test token");
		authServiceMock.logout("test token");
		EasyMock.replay(authServiceMock);

		crucibleSession.login(VALID_LOGIN, VALID_PASSWORD);
		crucibleSession.logout();
		crucibleSession.logout();

		crucibleSession = new CrucibleSessionImpl(VALID_URL);
		crucibleSession.logout();

		EasyMock.verify(authServiceMock);
	}

	public void testFailedCrucibleLogin() {
		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_URL);
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

	public void testSuccessCrucibleDoubleLogin() throws Exception {

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_URL);

		authServiceMock.login(VALID_LOGIN, VALID_PASSWORD);
		EasyMock.expectLastCall().andReturn("test token");
		EasyMock.replay(authServiceMock);

		crucibleSession.login(VALID_LOGIN, VALID_PASSWORD);
		try {
			crucibleSession.login(VALID_LOGIN, VALID_PASSWORD);
			fail();
		} catch (IllegalStateException ex) {
			//expected
		}


		EasyMock.verify(authServiceMock);
	}

	public void testMethodCallWithoutLogin() throws Exception {
		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_URL);
		List<State> states = new ArrayList<State>();
		try {
			crucibleSession.getReviewsInStates(states);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			crucibleSession.getAllReviews();
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			crucibleSession.getReviewers(null);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}

		try {
			crucibleSession.createReview(null);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}

		try {
			crucibleSession.createReviewFromPatch(null, "patch");
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
	}

	public void testGetReviewsInStates() throws Exception {

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(VALID_URL);

		authServiceMock.login(VALID_LOGIN, VALID_PASSWORD);
		EasyMock.expectLastCall().andReturn("test token");
		EasyMock.replay(authServiceMock);

		List<State> states = new ArrayList<State>();
		states.add(State.REVIEW);

		List<ReviewData> reviews = Arrays.asList(
				prepareReviewData("review1", State.REVIEW),
				prepareReviewData("review2", State.REVIEW));

		List<com.atlassian.theplugin.crucible.api.soap.xfire.review.State> cxfStates = new ArrayList<com.atlassian.theplugin.crucible.api.soap.xfire.review.State>();
		for (State s: states) {
			cxfStates.add(CrucibleSessionImpl.translateToCxfState(s));
		}

		List<com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData> cxfReviews = new ArrayList<com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData>();
		for (ReviewData rd : reviews) {
			cxfReviews.add(CrucibleSessionImpl.translateToCxfReviewData(rd));
		}

		reviewServiceMock.getReviewsInStates("test token", cxfStates);
		EasyMock.expectLastCall().andReturn(cxfReviews);
		EasyMock.replay(reviewServiceMock);

		crucibleSession.login(VALID_LOGIN, VALID_PASSWORD);
		List<ReviewData> result = crucibleSession.getReviewsInStates(states);
		assertNotNull(result);
		assertEquals(reviews.size(), result.size());
		for (int i = 0; i < reviews.size(); ++i) {
			assertEquals(reviews.get(i).getPermaId().getId(), result.get(i).getPermaId().getId());
			assertEquals(reviews.get(i).getState(), result.get(i).getState());
			assertEquals(reviews.get(i).getProjectKey(), result.get(i).getProjectKey());
			assertEquals(reviews.get(i).getName(), result.get(i).getName());
			assertEquals(reviews.get(i).getAuthor(), result.get(i).getAuthor());
			assertEquals(reviews.get(i).getCreator(), result.get(i).getCreator());			
			assertEquals(reviews.get(i).getDescription(), result.get(i).getDescription());
		}

		EasyMock.verify(reviewServiceMock);
		EasyMock.verify(authServiceMock);

	}

	private ReviewData prepareReviewData(final String name, final State state) {
		return new ReviewData() {
			public String getAuthor() {
				return VALID_LOGIN;
			}

			public String getCreator() {
				return VALID_LOGIN;
			}

			public String getDescription() {
				return "Test description";
			}

			public String getModerator() {
				return VALID_LOGIN;
			}

			public String getName() {
				return name;
			}

			public PermId getParentReview() {
				return null;
			}

			public PermId getPermaId() {
				return new PermId(){
					public String getId() {
						return name + "TestId";
					}
				};
			}

			public String getProjectKey() {
				return "TEST";
			}

			public String getRepoName() {
				return null;
			}

			public State getState() {
				return state;
			}
		};
	}
}
