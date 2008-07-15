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

package com.atlassian.theplugin.crucible.api.rest;

import com.atlassian.theplugin.bamboo.api.bamboomock.ErrorResponse;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.crucible.api.*;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewDataImpl;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.configuration.ServerBean;
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
 * Test case for {#link BambooSessionImpl}
 */
public class CrucibleSessionTest extends TestCase {
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";

	private Server server;
	private JettyMockServer mockServer;
	private String mockBaseUrl;

	protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean());        

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
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

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
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		try {
			apiHandler.login(USER_NAME, PASSWORD);
			fail();
		} catch (RemoteApiException e) {

		}
	}

	public void testLoginInternalErrorResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new ErrorResponse(500, ""));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		try {
			apiHandler.login(USER_NAME, PASSWORD);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
	}

	public void testSuccessBambooLoginURLWithSlash() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));

		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl + "/");
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
		apiHandler.login(USER_NAME, PASSWORD);
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
            com.atlassian.theplugin.commons.Server srv = new ServerBean();
		    srv.setUrlString(mockBaseUrl);
		    CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login(null, null);
			fail();
		} catch (RemoteApiException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testNullLoginLogin() throws Exception {
		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString(mockBaseUrl);
			CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login(null, null);
			fail();
		} catch (RemoteApiLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testWrongUrlBambooLogin() throws Exception {
		ErrorResponse error = new ErrorResponse(400, "Bad Request");
		mockServer.expect("/wrongurl/rest-service/auth-v1/login", error);
		RemoteApiLoginException exception = null;

		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString(mockBaseUrl + "/wrongurl");
			CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login(USER_NAME, PASSWORD);
		} catch (RemoteApiLoginException ex) {
			exception = ex;
		}
		mockServer.verify();

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertSame(IOException.class, exception.getCause().getClass());
		assertTrue(exception.getMessage().startsWith(error.getErrorMessage()));
	}

	public void testNonExistingServerBambooLogin() throws Exception {
		RemoteApiLoginException exception = null;

		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString("http://non.existing.server.utest");
			CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login(USER_NAME, PASSWORD);
		} catch (RemoteApiLoginException ex) {
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
		RemoteApiException exception = null;
		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString(url);
			CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login(USER_NAME, PASSWORD);
		} catch (RemoteApiLoginException e) {
			exception = e;
		} catch (RemoteApiException e) {
			exception = e;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertTrue("MalformedURLException expected", exception.getCause() instanceof MalformedURLException);
		assertEquals("Malformed server URL: " + url, exception.getMessage());
	}

	public void testOutOfRangePort() {
		String url = "http://localhost:80808";
		RemoteApiException exception = null;
		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString(url);
			CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login(USER_NAME, PASSWORD);
		} catch (RemoteApiException e) {
			exception = e;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertTrue("MalformedURLException expected", exception.getCause() instanceof IOException);
	}


	public void testWrongUserCrucibleLogin() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString(mockBaseUrl);
			CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login(USER_NAME, PASSWORD); // mock will fail this
			fail();
		} catch (RemoteApiLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		mockServer.verify();
	}


	public void testWrongParamsCrucibleLogin() throws Exception {
		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString("");
			CrucibleSession apiHandler = new CrucibleSessionImpl(srv);
			apiHandler.login("", "");
			fail();
		} catch (RemoteApiException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testSuccessCrucibleLogout() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));

		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		assertTrue(apiHandler.isLoggedIn());

		apiHandler.logout();
		apiHandler.logout();

		com.atlassian.theplugin.commons.Server srv2 = new ServerBean();
		srv2.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler2 = new CrucibleSessionImpl(srv2);
		apiHandler2.logout();

		mockServer.verify();
	}

	public void testFailedCrucibleLogin() {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));
		CrucibleSession apiHandler = null;
		try {
			com.atlassian.theplugin.commons.Server srv = new ServerBean();
			srv.setUrlString(mockBaseUrl);
			apiHandler = new CrucibleSessionImpl(srv);
		} catch (RemoteApiException e) {
			fail();
		}

		try {

			apiHandler.login(USER_NAME, PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (RemoteApiLoginException e) {
			// expected
		}

		try {
			apiHandler.login(null, PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (RemoteApiLoginException e) {
			// expected
		}

		try {
			apiHandler.login(USER_NAME, null);
			fail("Login succeeded while expected failure.");
		} catch (RemoteApiLoginException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testSuccessCrucibleDoubleLogin() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		apiHandler.login(USER_NAME, PASSWORD);

		mockServer.verify();
	}

	public void testMethodCallWithoutLogin() throws Exception {
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession crucibleSession = new CrucibleSessionImpl(srv);
		List<State> states = new ArrayList<State>();
		try {
			crucibleSession.getReviewsInStates(states, false);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			crucibleSession.getAllReviews(false);
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
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getAllReviews(false);
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
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getAllReviews(false);
		assertEquals(states.size(), reviews.size());
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetEmptyReviewsForType() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = new ArrayList<State>();
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getAllReviews(false);
		assertEquals(states.size(), reviews.size());
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetReviewsInStates() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<ReviewData> reviews = apiHandler.getReviewsInStates(states, false);
		assertEquals(states.size(), reviews.size());
		assertTrue(!reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetMissingReviewsInStates() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<State> req = Arrays.asList(State.CLOSED);
		List<ReviewData> reviews = apiHandler.getReviewsInStates(req, false);
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetEmptyRequestReviewsInStates() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/reviews-v1", new GetReviewsCallback(states));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<State> req = Arrays.asList();
		List<ReviewData> reviews = apiHandler.getReviewsInStates(req, false);
		assertEquals(states.size(), reviews.size());
		assertTrue(!reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetAllReviewsMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		try {
			apiHandler.getAllReviews(false);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testGetReviewsInStatesMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		try {
			List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
			apiHandler.getReviewsInStates(states, false);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testGetEmptyReviewers() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new GetReviewersCallback(new User[]{ }));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		List<Reviewer> reviewers = apiHandler.getReviewers(permId);
		assertEquals(0, reviewers.size());
		mockServer.verify();
	}

	public void testGetReviewers() throws Exception {
        UserBean[] reviewers = new UserBean[3];
        reviewers[0] = new UserBean();
        reviewers[0].setUserName("bob");
        reviewers[1] = new UserBean();
        reviewers[1].setUserName("alice");
        reviewers[2] = new UserBean();
        reviewers[2].setUserName("steve");

        mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new GetReviewersCallback(reviewers));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		List<Reviewer> result = apiHandler.getReviewers(permId);
		assertEquals(3, result.size());
		assertEquals(result.get(0).getUserName(), "bob");
		assertEquals(result.get(1).getUserName(), "alice");
		assertEquals(result.get(2).getUserName(), "steve");        
		mockServer.verify();
	}

	public void testGetReviewersInvalidId() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-2/reviewers", new ErrorResponse(500, ""));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-2");
		try {
			apiHandler.getReviewers(permId);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testGetReviewersMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new MalformedResponseCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		PermIdBean permId = new PermIdBean();
		permId.setId("PR-1");
		try {
			apiHandler.getReviewers(permId);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testCreateReview() throws Exception {
		ReviewData review = createReviewRequest();

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		Review response = apiHandler.createReview(review);
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewMalformedResponse() throws Exception {
		ReviewData review = createReviewRequest();

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		try {
			apiHandler.createReview(review);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testCreateReviewErrorResponse() throws Exception {
		ReviewData review = createReviewRequest();

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new ErrorResponse(500, ""));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		try {
			apiHandler.createReview(review);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}


	public void testCreateReviewFromPatch() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		ReviewData review = createReviewRequest();
		Review response = apiHandler.createReviewFromPatch(review, "patch text");
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromNullPatch() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		ReviewData review = createReviewRequest();
		Review response = apiHandler.createReviewFromPatch(review, null);
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromEmptyPatch() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		ReviewData review = createReviewRequest();
		Review response = apiHandler.createReviewFromPatch(review, "");
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(CreateReviewCallback.REPO_NAME, response.getRepoName());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromPatchMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		try {
			ReviewData review = createReviewRequest();
			apiHandler.createReviewFromPatch(review, "patch text");
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		
		mockServer.verify();
	}

	public void testGetProjects() throws Exception {
		int size = 4;

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/projects-v1", new GetProjectsCallback(size));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<Project> project = apiHandler.getProjects();
		assertEquals(size, project.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals(id, project.get(i).getId());
			assertEquals("ProjectName" + id, project.get(i).getName());
			assertEquals("CR" + id, project.get(i).getKey());
		}
		mockServer.verify();
	}

	public void testGetProjectsEmpty() throws Exception {
		int size = 0;

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/projects-v1", new GetProjectsCallback(size));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<Project> project = apiHandler.getProjects();
		assertEquals(size, project.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals(id, project.get(i).getId());
			assertEquals("ProjectName" + id, project.get(i).getName());
			assertEquals("CR" + id, project.get(i).getKey());
		}
		mockServer.verify();
	}

	public void testGetRepositories() throws Exception {
		int size = 4;

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/repositories-v1", new GetRepositoriesCallback(size));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<Repository> repositories = apiHandler.getRepositories();
		assertEquals(size, repositories.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals("RepoName" + id, repositories.get(i).getName());
		}
		mockServer.verify();
	}

	public void testGetRepositoriesEmpty() throws Exception {
		int size = 0;

		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/repositories-v1", new GetRepositoriesCallback(size));
		com.atlassian.theplugin.commons.Server srv = new ServerBean();
		srv.setUrlString(mockBaseUrl);
		CrucibleSession apiHandler = new CrucibleSessionImpl(srv);

		apiHandler.login(USER_NAME, PASSWORD);
		List<Repository> repositories = apiHandler.getRepositories();
		assertEquals(size, repositories.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals("RepoName" + id, repositories.get(i).getName());
		}
		mockServer.verify();
	}

	private ReviewData createReviewRequest() {
		ReviewDataImpl review = new ReviewDataImpl(new ServerBean());
		review.setAuthor(new UserBean("autor",""));
		review.setCreator(new UserBean("creator",""));
		review.setDescription("description");
		review.setModerator(new UserBean("moderator",""));
		review.setName("name");
		review.setProjectKey("PR");
		return review;
	}
}