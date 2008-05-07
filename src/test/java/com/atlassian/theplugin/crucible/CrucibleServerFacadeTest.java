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

package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.api.*;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.LoginCallback;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import org.mortbay.jetty.Server;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrucibleServerFacadeTest extends TestCase {
	private static final String VALID_LOGIN = "validLogin";
	private static final String VALID_PASSWORD = "validPassword";
	private static final String VALID_URL = "http://localhost:9001";

	private CrucibleServerFacade facade;
	private CrucibleSession crucibleSessionMock;
	public static final String INVALID_PROJECT_KEY = "INVALID project key";

	@SuppressWarnings("unchecked")
	protected void setUp() {

		crucibleSessionMock = createMock(CrucibleSession.class);

		facade = CrucibleServerFacadeImpl.getInstance();

		try {
			Field f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
			f.setAccessible(true);

			((Map<String, CrucibleSession>) f.get(facade)).put(VALID_URL + VALID_LOGIN + VALID_PASSWORD, crucibleSessionMock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testConnectionTestFailed() throws Exception {

		Server server = new Server(0);
		server.start();

		String mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();
		JettyMockServer mockServer = new JettyMockServer(server);
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(VALID_LOGIN, VALID_PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			facade.testServerConnection(mockBaseUrl, VALID_LOGIN, VALID_PASSWORD);
			fail("testServerConnection failed");
		} catch (RemoteApiException e) {
			//
		}

		mockServer.verify();
		mockServer = null;
		server.stop();		
	}

	public void testConnectionTestSucceed() throws Exception {
		Server server = new Server(0);
		server.start();

		String mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();
		JettyMockServer mockServer = new JettyMockServer(server);
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(VALID_LOGIN, VALID_PASSWORD));

		try {
			facade.testServerConnection(mockBaseUrl, VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiException e) {
			fail("testServerConnection failed");
		}

		mockServer.verify();
		mockServer = null;
		server.stop();
	}

	public void testChangedCredentials() throws Exception {
		String validLogin2 = VALID_LOGIN + 2;
		String validPassword2 = VALID_PASSWORD + 2;
		try {
			Field f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
			f.setAccessible(true);

			((Map<String, CrucibleSession>) f.get(facade)).put(VALID_URL + validLogin2 + validPassword2, crucibleSessionMock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		PermId permId = new PermId() {
			public String getId() {
				return "permId";
			}
		};

		ReviewDataInfoImpl review = new ReviewDataInfoImpl(prepareReviewData(VALID_LOGIN, "name", State.DRAFT, permId), null, null);

		crucibleSessionMock.getAllReviews();
		EasyMock.expectLastCall().andReturn(Arrays.asList(review, review));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));
		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));

		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);		
		try {
			crucibleSessionMock.login(validLogin2, validPassword2);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ReviewDataInfoImpl review2 = new ReviewDataInfoImpl(prepareReviewData(validLogin2, "name", State.DRAFT, permId), null, null);
		crucibleSessionMock.getAllReviews();
		EasyMock.expectLastCall().andReturn(Arrays.asList(review2));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		List<ReviewDataInfo> ret = facade.getAllReviews(server);
		assertEquals(2, ret.size());
		assertEquals(3, ret.get(0).getReviewers().size());
		assertEquals(permId.getId(), ret.get(0).getPermaId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(VALID_LOGIN, ret.get(0).getAuthor());
		assertEquals(VALID_LOGIN, ret.get(0).getCreator());
		assertEquals("Test description", ret.get(0).getDescription());
		assertEquals(VALID_LOGIN, ret.get(0).getModerator());
		assertEquals("TEST", ret.get(0).getProjectKey());
		assertEquals(null, ret.get(0).getRepoName());
		assertEquals(Arrays.asList("Alice", "Bob", "Charlie"), ret.get(0).getReviewers());
		assertEquals(VALID_URL + "/cru/permId", ret.get(0).getReviewUrl());
		assertSame(server, ret.get(0).getServer());
		assertSame(State.DRAFT, ret.get(0).getState());
		assertNull(ret.get(0).getParentReview());

		server.setUserName(validLogin2);
		server.transientSetPasswordString(validPassword2, false);
		ret = facade.getAllReviews(server);
		assertEquals(1, ret.size());
		assertEquals(3, ret.get(0).getReviewers().size());
		assertEquals(permId.getId(), ret.get(0).getPermaId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(validLogin2, ret.get(0).getAuthor());
		assertEquals(validLogin2, ret.get(0).getCreator());
		assertEquals("Test description", ret.get(0).getDescription());
		assertEquals(validLogin2, ret.get(0).getModerator());
		assertEquals("TEST", ret.get(0).getProjectKey());
		assertEquals(null, ret.get(0).getRepoName());
		assertEquals(Arrays.asList("Alice", "Bob", "Charlie"), ret.get(0).getReviewers());
		assertEquals(VALID_URL + "/cru/permId", ret.get(0).getReviewUrl());
		assertSame(server, ret.get(0).getServer());
		assertSame(State.DRAFT, ret.get(0).getState());
		assertNull(ret.get(0).getParentReview());

		EasyMock.verify(crucibleSessionMock);
	}

	public void testCreateReview() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReview(EasyMock.isA(ReviewData.class));
		ReviewData response = new ReviewDataInfoImpl(null, null, null);

		EasyMock.expectLastCall().andReturn(response);

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		// test call
		ReviewData ret = facade.createReview(server, reviewData);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);


	}

	public void testCreateReviewWithInvalidProjectKey() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReview(EasyMock.isA(ReviewData.class));

		EasyMock.expectLastCall().andThrow(new RemoteApiException("test"));

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		try {
			// test call
			facade.createReview(server, reviewData);
			fail("creating review with invalid key should throw an CrucibleException()");
		} catch (RemoteApiException e) {

		} finally {
			EasyMock.verify(crucibleSessionMock);
		}

	}

	public void testCreateReviewFromPatch() throws ServerPasswordNotProvidedException, RemoteApiException {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(ReviewData.class), EasyMock.eq("some patch"));
		ReviewData response = new ReviewDataInfoImpl(null, null, null);
		EasyMock.expectLastCall().andReturn(response);

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		String patch = "some patch";

		// test call
		ReviewData ret = facade.createReviewFromPatch(server, reviewData, patch);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);
	}

	public void testCreateReviewFromPatchWithInvalidProjectKey() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(ReviewData.class), EasyMock.eq("some patch"));
		EasyMock.expectLastCall().andThrow(new RemoteApiException("test"));

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		String patch = "some patch";

		try {
			facade.createReviewFromPatch(server, reviewData, patch);
			fail("creating review with patch with invalid key should throw an RemoteApiException()");
		} catch (RemoteApiException e) {
			// ignored by design
		} finally {
			EasyMock.verify(crucibleSessionMock);
		}
	}

	public void testGetAllReviews() throws ServerPasswordNotProvidedException, RemoteApiException {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		PermId permId = new PermId() {
			public String getId() {
				return "permId";
			}
		};

		ReviewDataInfoImpl review = new ReviewDataInfoImpl(prepareReviewData(VALID_LOGIN, "name", State.DRAFT, permId), null, null);

		crucibleSessionMock.getAllReviews();
		EasyMock.expectLastCall().andReturn(Arrays.asList(review, review));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));
		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		// test call
		List<ReviewDataInfo> ret = facade.getAllReviews(server);
		assertEquals(2, ret.size());
		assertEquals(3, ret.get(0).getReviewers().size());
		assertEquals(permId.getId(), ret.get(0).getPermaId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(VALID_LOGIN, ret.get(0).getAuthor());
		assertEquals(VALID_LOGIN, ret.get(0).getCreator());
		assertEquals("Test description", ret.get(0).getDescription());
		assertEquals(VALID_LOGIN, ret.get(0).getModerator());
		assertEquals("TEST", ret.get(0).getProjectKey());
		assertEquals(null, ret.get(0).getRepoName());
		assertEquals(Arrays.asList("Alice", "Bob", "Charlie"), ret.get(0).getReviewers());
		assertEquals(VALID_URL + "/cru/permId", ret.get(0).getReviewUrl());
		assertSame(server, ret.get(0).getServer());
		assertSame(State.DRAFT, ret.get(0).getState());
		assertNull(ret.get(0).getParentReview());
		
		EasyMock.verify(crucibleSessionMock);
	}

	public void testGetActiveReviewsForUser() throws ServerPasswordNotProvidedException, RemoteApiException {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		PermId permId = new PermId() {
			public String getId() {
				return "permId";
			}
		};

		crucibleSessionMock.getReviewsInStates(Arrays.asList(State.REVIEW));
		EasyMock.expectLastCall().andReturn(Arrays.asList(
				new ReviewDataInfoImpl(prepareReviewData(VALID_LOGIN, "name", State.REVIEW, permId), null, null),
				new ReviewDataInfoImpl(prepareReviewData(VALID_LOGIN, "name", State.REVIEW, permId), null, null)));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList(VALID_LOGIN, "Bob", "Charlie"));
		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		// test call
		List<ReviewDataInfo> ret = facade.getActiveReviewsForUser(server);
		assertEquals(1, ret.size());
		assertEquals(3, ret.get(0).getReviewers().size());
		assertEquals(permId.getId(), ret.get(0).getPermaId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(VALID_LOGIN, ret.get(0).getReviewers().get(0));

		EasyMock.verify(crucibleSessionMock);
	}

	public void testGetProjects() throws ServerPasswordNotProvidedException, RemoteApiException {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}
		crucibleSessionMock.getProjects();
		EasyMock.expectLastCall().andReturn(Arrays.asList(prepareProjectData(0), prepareProjectData(1)));
		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		// test call
		List<ProjectData> ret = facade.getProjects(server);
		assertEquals(2, ret.size());
		for (int i = 0; i < 2; i++) {
			String id = Integer.toString(i);
			assertEquals(id, ret.get(i).getId());
			assertEquals("CR" + id, ret.get(i).getKey());
			assertEquals("Name" + id, ret.get(i).getName());
		}		
		EasyMock.verify(crucibleSessionMock);
	}

	public void testGetRepositories() throws ServerPasswordNotProvidedException, RemoteApiException {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}
		crucibleSessionMock.getRepositories();
		EasyMock.expectLastCall().andReturn(Arrays.asList(prepareRepositoryData(0), prepareRepositoryData(1)));
		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		// test call
		List<RepositoryData> ret = facade.getRepositories(server);
		assertEquals(2, ret.size());
		for (int i = 0; i < 2; i++) {
			String id = Integer.toString(i);
			assertEquals("RepoName" + id, ret.get(i).getName());
		}
		EasyMock.verify(crucibleSessionMock);
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
				return new PermId() {
					public String getId() {
						return "permId";
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

	private ReviewData prepareReviewData(final String user, final String name, final State state, final PermId permId) {
		return new ReviewData() {
			public String getAuthor() {
				return user;
			}

			public String getCreator() {
				return user;
			}

			public String getDescription() {
				return "Test description";
			}

			public String getModerator() {
				return user;
			}

			public String getName() {
				return name;
			}

			public PermId getParentReview() {
				return null;
			}

			public PermId getPermaId() {
				return permId;
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

	private ServerBean prepareServerBean() {
		ServerBean server = new ServerBean();
		server.setUrlString(VALID_URL);
		server.setUserName(VALID_LOGIN);
		server.transientSetPasswordString(VALID_PASSWORD, false);
		return server;
	}

	private ProjectData prepareProjectData(final int i) {
		return new ProjectData() {
			public String getId() {
				return Integer.toString(i);
			}

			public String getKey() {
				return "CR" + Integer.toString(i);
			}

			public String getName() {
				return "Name" + Integer.toString(i);
			}
		};
	}

	private RepositoryData prepareRepositoryData(final int i) {
		return new RepositoryData() {
			public String getName() {
				return "RepoName" + Integer.toString(i);
			}
		};
	}

	public void _testCreateReviewHardcoded() throws ServerPasswordNotProvidedException {

		//facade.setCrucibleSession(null);

		ServerBean server = new ServerBean();
		server.setUrlString("http://lech.atlassian.pl:8060");
		server.setUserName("test");
		server.transientSetPasswordString("test", false);

		ReviewData reviewData = prepareReviewData("test", State.DRAFT);

		ReviewData ret;

		try {
			ret = facade.createReview(server, reviewData);
			assertNotNull(ret);
			assertNotNull(ret.getPermaId());
			assertNotNull(ret.getPermaId().getId());
			assertTrue(ret.getPermaId().getId().length() > 0);
		} catch (RemoteApiException e) {
			fail(e.getMessage());
		}
	}

	public void _testGetAllReviewsHardcoded() throws ServerPasswordNotProvidedException {
		//facade.setCrucibleSession(null);

		ServerBean server = new ServerBean();
		server.setUrlString("http://lech.atlassian.pl:8060");
		server.setUserName("test");
		server.transientSetPasswordString("test", false);

		try {
			List<ReviewDataInfo> list = facade.getAllReviews(server);
			assertNotNull(list);
			assertTrue(list.size() > 0);
		} catch (RemoteApiException e) {
			fail(e.getMessage());
		}
	}
}
