package com.atlassian.theplugin.crucible.api.rest;

import com.atlassian.theplugin.bamboo.api.bamboomock.ErrorResponse;
import com.atlassian.theplugin.crucible.api.*;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.*;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.mortbay.jetty.Server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Test case for {#link BambooSession}
 */
public class CrucibleSessionTest extends TestCase {
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";

	private Server server;
	private JettyMockServer mockServer;
	private String mockBaseUrl;

	protected void setUp() throws Exception {
		server = new Server(0);
		server.start();

		mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(server);
	}

	protected void tearDown() throws Exception {
		mockServer.verify();
		mockServer = null;
		mockBaseUrl = null;
		server.stop();
	}

	public void testSuccessCrucibleLogin() throws Exception {

		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		String[] usernames = { "user", "+-=&;<>", "", "a;&username=other", "!@#$%^&*()_-+=T " };
		String[] passwords = { "password", "+-=&;<>", "", "&password=other", ",./';[]\t\\ |}{\":><?" };

		for (int i = 0; i < usernames.length; ++i) {
			mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(usernames[i], passwords[i]));

			apiHandler.login(usernames[i], passwords[i]);
			assertTrue(apiHandler.isLoggedIn());
			apiHandler.logout();
			assertFalse(apiHandler.isLoggedIn());
		}

		mockServer.verify();
	}

	public void testLoginMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new MalformedResponseCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		try {
			apiHandler.login(USER_NAME, PASSWORD);
			fail();
		} catch (CrucibleException e) {

		}
	}

	public void testLoginInternalErrorResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new ErrorResponse(500));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		try {
			apiHandler.login(USER_NAME, PASSWORD);
			fail();
		} catch (CrucibleException e) {
			// expected
		}
	}

	public void testSuccessBambooLoginURLWithSlash() throws Exception {
		mockServer.expect("//rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));

		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD);
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
			CrucibleSession apiHandler = new CrucibleSessionImpl(null);
			apiHandler.login(null, null);
			fail();
		} catch (CrucibleLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testNullLoginLogin() throws Exception {
		try {
			CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);
			apiHandler.login(null, null);
			fail();
		} catch (CrucibleLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testWrongUrlBambooLogin() throws Exception {
		mockServer.expect("/wrongurl/rest-service/auth-v1/login", new ErrorResponse(400));
		CrucibleLoginException exception = null;

		try {
			CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl + "/wrongurl");
			apiHandler.login(USER_NAME, PASSWORD);
		} catch (CrucibleLoginException ex) {
			exception = ex;
		}
		mockServer.verify();

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertSame(IOException.class, exception.getCause().getClass());
		assertTrue(exception.getMessage().startsWith(ErrorResponse.getErrorMessage()));
	}

	public void testNonExistingServerBambooLogin() throws Exception {
		CrucibleLoginException exception = null;

		try {
			CrucibleSession apiHandler = new CrucibleSessionImpl("http://non.existing.server.utest");
			apiHandler.login(USER_NAME, PASSWORD);
		} catch (CrucibleLoginException ex) {
			exception = ex;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertSame("UnknownHostException expected", UnknownHostException.class, exception.getCause().getClass());
		assertEquals("Checking exception message", "Unknown host: non.existing.server.utest", exception.getMessage());
	}

	public void testMalformedUrlCrucibleLogin() {
		tryMalformedUrl("noprotocol.url/path");
		tryMalformedUrl("http:localhost/path");
		tryMalformedUrl("http:/localhost/path");
		tryMalformedUrl("http:///localhost/path");
		tryMalformedUrl("http:localhost");
		tryMalformedUrl("http:/localhost");
		tryMalformedUrl("http:///localhost");
		tryMalformedUrl("http://");
		tryMalformedUrl("ncxvx:/localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("ncxvx://localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("https:localhost/path");
		tryMalformedUrl("https:/localhost/path");
		tryMalformedUrl("https:///localhost/path");
		tryMalformedUrl("https:localhost");
		tryMalformedUrl("https:/localhost");
		tryMalformedUrl("https:///localhost");
		tryMalformedUrl("https://");
		tryMalformedUrl("http::localhost/path");
		tryMalformedUrl("http://loca:lhost/path");
	}

	private void tryMalformedUrl(final String url) {
		CrucibleLoginException exception = null;
		try {
			CrucibleSession apiHandler = new CrucibleSessionImpl(url);
			apiHandler.login(USER_NAME, PASSWORD);
		} catch (CrucibleLoginException e) {
			exception = e;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertTrue("MalformedURLException expected", exception.getCause() instanceof MalformedURLException);
		assertEquals("Malformed server URL: " + url, exception.getMessage());
	}

	public void testWrongUserCrucibleLogin() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);
			apiHandler.login(USER_NAME, PASSWORD); // mock will fail this
			fail();
		} catch (CrucibleLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		mockServer.verify();
	}


	public void testWrongParamsCrucibleLogin() throws Exception {
		try {
			CrucibleSession apiHandler = new CrucibleSessionImpl("");
			apiHandler.login("", "");
			fail();
		} catch (CrucibleLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testSuccessCrucibleLogout() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));

		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		assertTrue(apiHandler.isLoggedIn());

		apiHandler.logout();
		apiHandler.logout();

		CrucibleSession apiHandler2 = new CrucibleSessionImpl(mockBaseUrl);
		apiHandler2.logout();

		mockServer.verify();
	}

	public void testFailedCrucibleLogin() {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		try {
			apiHandler.login(USER_NAME, PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {
			// expected
		}

		try {
			apiHandler.login(null, PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {
			// expected
		}

		try {
			apiHandler.login(USER_NAME, null);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testSuccessCrucibleDoubleLogin() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		apiHandler.login(USER_NAME, PASSWORD);

		mockServer.verify();
	}

	public void testMethodCallWithoutLogin() throws Exception {
		CrucibleSession crucibleSession = new CrucibleSessionImpl(mockBaseUrl);
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

	public void testGetAllTypeReviews() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.values());
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getAllReviews();
		assertEquals(states.size(), reviews.size());
		int i = 0;
		for (ReviewData review : reviews) {
			assertEquals(review.getState(), states.get(i++));
		}
		mockServer.verify();
	}

	public void testGetEmptyReviews() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = new ArrayList<State>();
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getAllReviews();
		assertEquals(states.size(), reviews.size());
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetEmptyReviewsForType() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = new ArrayList<State>();
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getAllReviews();
		assertEquals(states.size(), reviews.size());
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetReviewsInStates() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getReviewsInStates(states);
		assertEquals(states.size(), reviews.size());
		assertTrue(!reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetMissingReviewsInStates() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		List<State> req = Arrays.asList(State.CLOSED);
		List<ReviewData> reviews = apiHandler.getReviewsInStates(req);
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetEmptyRequestReviewsInStates() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		List<State> req = Arrays.asList();
		List<ReviewData> reviews = apiHandler.getReviewsInStates(req);
		assertEquals(states.size(), reviews.size());
		assertTrue(!reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetAllReviewsMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		try {
			apiHandler.getAllReviews();
			fail();
		} catch (CrucibleException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testGetReviewsInStatesMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		try {
			List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
			apiHandler.getReviewsInStates(states);
			fail();
		} catch (CrucibleException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testGetEmptyReviewers() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new GetReviewersCallback(new String[]{ }));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		List<String> reviewers = apiHandler.getReviewers(permId);
		assertEquals(0, reviewers.size());
		mockServer.verify();
	}

	public void testGetReviewers() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new GetReviewersCallback(new String[]{ "bob", "alice", "steve" }));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		List<String> reviewers = apiHandler.getReviewers(permId);
		assertEquals(3, reviewers.size());
		assertTrue(reviewers.contains("bob"));
		assertTrue(reviewers.contains("alice"));
		assertTrue(reviewers.contains("steve"));
		assertTrue(!reviewers.contains("tom"));
		mockServer.verify();
	}

	public void testGetReviewersInvalidId() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-2/reviewers", new ErrorResponse(500));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-2");
		try {
			apiHandler.getReviewers(permId);
			fail();
		} catch (CrucibleException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testGetReviewersMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new MalformedResponseCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		try {
			apiHandler.getReviewers(permId);
			fail();
		} catch (CrucibleException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testCreateReview() throws Exception {
		ReviewDataBean review = createReviewRequest();

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		ReviewData response = apiHandler.createReview(review);
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermaId().getId());

		mockServer.verify();
	}

	public void testCreateReviewMalformedResponse() throws Exception {
		ReviewDataBean review = createReviewRequest();

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		try {
			apiHandler.createReview(review);
			fail();
		} catch (CrucibleException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testCreateReviewErrorResponse() throws Exception {
		ReviewDataBean review = createReviewRequest();

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new ErrorResponse(500));
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		try {
			apiHandler.createReview(review);
			fail();
		} catch (CrucibleException e) {
			// expected
		}

		mockServer.verify();
	}


	public void testCreateReviewFromPatch() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		ReviewDataBean review = createReviewRequest();
		ReviewData response = apiHandler.createReviewFromPatch(review, "patch text");
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermaId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromNullPatch() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		ReviewDataBean review = createReviewRequest();
		ReviewData response = apiHandler.createReviewFromPatch(review, null);
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermaId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromEmptyPatch() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		ReviewDataBean review = createReviewRequest();
		ReviewData response = apiHandler.createReviewFromPatch(review, "");
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermaId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromPatchMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		CrucibleSession apiHandler = new CrucibleSessionImpl(mockBaseUrl);

		apiHandler.login(USER_NAME, PASSWORD);
		try {
			ReviewDataBean review = createReviewRequest();
			apiHandler.createReviewFromPatch(review, "patch text");
			fail();
		} catch (CrucibleException e) {
			// expected
		}
		
		mockServer.verify();
	}

	private ReviewDataBean createReviewRequest() {
		ReviewDataBean review = new ReviewDataBean();
		review.setAuthor("autor");
		review.setCreator("creator");
		review.setDescription("description");
		review.setModerator("moderator");
		review.setName("name");
		review.setProjectKey("PR");
		return review;
	}
}